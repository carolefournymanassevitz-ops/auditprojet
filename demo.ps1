# Démonstration de la PoC avec de vrais jetons Keycloak.
# Prérequis : `docker compose up -d` (Oracle + Keycloak prêts) et l'application démarrée (voir README).

$racine = $PSScriptRoot
Get-Content "$racine\.env" | Where-Object { $_ -match '^\s*[A-Z_]+=' } | ForEach-Object {
    $nom, $valeur = $_ -split '=', 2
    Set-Item "env:$nom" $valeur
}

$api = 'http://localhost:8090'
$prmAccepte = '30001000000001'
$prmRefuse = '30001000000002'
$prmSansReponse = '30001000000009'

function Obtenir-Jeton([string] $utilisateur) {
    $reponse = Invoke-RestMethod -Method Post -Uri 'http://localhost:8180/realms/poc/protocol/openid-connect/token' -Body @{
        grant_type = 'password'; client_id = 'poc-portail'; username = $utilisateur; password = $env:DEMO_USER_PASSWORD
    }
    return $reponse.access_token
}

function Lire-Revendications([string] $jeton) {
    $charge = $jeton.Split('.')[1].Replace('-', '+').Replace('_', '/')
    $charge += '=' * ((4 - $charge.Length % 4) % 4)
    return [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($charge)) | ConvertFrom-Json
}

function Appeler([string] $titre, [string] $methode, [string] $chemin, [string] $jeton, [string] $corps) {
    $entetes = @{}
    if ($jeton) { $entetes.Authorization = "Bearer $jeton" }
    $parametres = @{ UseBasicParsing = $true; Method = $methode; Uri = "$api$chemin"; Headers = $entetes }
    if ($corps) { $parametres.ContentType = 'application/json'; $parametres.Body = [Text.Encoding]::UTF8.GetBytes($corps) }
    try {
        $reponse = Invoke-WebRequest @parametres
        $code = [int] $reponse.StatusCode
        $contenu = $reponse.Content
    } catch {
        $code = [int] $_.Exception.Response.StatusCode
        $contenu = $_.ErrorDetails.Message
    }
    Write-Host ('{0,-62} -> {1}' -f $titre, $code)
    return $contenu
}

# Le SQL passe en base64 dans la ligne de commande : Windows PowerShell 5.1 préfixe un BOM à tout ce qu'il envoie
# sur l'entrée standard d'un programme, que sqlplus refuse. Le mot de passe est lu dans le conteneur, jamais affiché.
function Executer-Sql([string] $compte, [string] $variableMotDePasse, [string] $sql) {
    $base64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($sql))
    docker compose -f "$racine\docker-compose.yml" exec -T oracle bash -c "echo $base64 | base64 -d | sqlplus -s $compte/`${$variableMotDePasse}@//localhost/FREEPDB1" | Out-Host
}

function Corps([string] $prm, [string] $parametres = '{}', [string] $enPlus = '') {
    return "{`"prm`":`"$prm`",`"service`":`"LECTURE_DONNEES`",`"parametres`":$parametres$enPlus}"
}

$operateurA = Obtenir-Jeton 'operateur.a'
$lecteurA = Obtenir-Jeton 'lecteur.a'
$operateurB = Obtenir-Jeton 'operateur.b'

Write-Host "`n=== Revendications du jeton de operateur.a ==="
$revendications = Lire-Revendications $operateurA
Write-Host ("sub = {0} ; utilisateur = {1} ; designation = {2} ; roles = {3}" -f $revendications.sub,
    $revendications.preferred_username, ($revendications.designation -join ','), ($revendications.realm_access.roles -join ','))

Write-Host "`n=== Refus : contrôlés par le serveur, rien n'est journalisé ==="
$null = Appeler '1. Sans jeton' Post '/teleoperations/mono' $null (Corps $prmAccepte)
$null = Appeler '2. Compte lecture seule (ELD_A)' Post '/teleoperations/mono' $lecteurA (Corps $prmAccepte)
$null = Appeler '3. Opérateur ELD_B sur un compteur ELD_A' Post '/teleoperations/mono' $operateurB (Corps $prmAccepte)
$null = Appeler '4. Auteur fourni dans le corps' Post '/teleoperations/mono' $operateurA (Corps $prmAccepte '{}' ',"auteur":"quelqu.un"')

Write-Host "`n=== Lancements autorisés : une demande puis un résultat, toujours ==="
$null = Appeler '5. Système central accepte (avec une clé à masquer)' Post '/teleoperations/mono' $operateurA (Corps $prmAccepte '{"cleAdmin":"VALEUR-SECRETE-123"}')
$null = Appeler '6. Système central refuse' Post '/teleoperations/mono' $operateurA (Corps $prmRefuse)
$null = Appeler '7. Système central ne répond pas' Post '/teleoperations/mono' $operateurA (Corps $prmSansReponse)

Write-Host "`n=== Lecture du journal, cloisonnée par ELD ==="
$journalA = Appeler '8. Lecteur ELD_A consulte le journal du compteur' Get "/teleoperations/journal?prm=$prmAccepte" $lecteurA $null
$journalB = Appeler '9. Opérateur ELD_B consulte le même compteur' Get "/teleoperations/journal?prm=$prmAccepte" $operateurB $null
# (ConvertFrom-Json $x).Count et non @($x | ConvertFrom-Json).Count : PowerShell 5.1 ne déplie pas le tableau dans un pipeline.
Write-Host ("   entrées vues par ELD_A : {0} ; par ELD_B : {1}" -f (ConvertFrom-Json $journalA).Count, (ConvertFrom-Json $journalB).Count)

Write-Host "`n=== Contenu réel de PORTAL.AUDIT_EVENT (compte propriétaire) ==="
Executer-Sql 'portal' 'PORTAL_PASSWORD' @'
SET LINESIZE 220 PAGESIZE 100 FEEDBACK OFF
COLUMN UTC FORMAT A8
COLUMN ACTOR_NAME FORMAT A12
COLUMN TARGET_PRM FORMAT A15
COLUMN OUTCOME FORMAT A8
COLUMN CORRELATION FORMAT A11
COLUMN DETAILS FORMAT A90
SELECT EVENT_ID, TO_CHAR(OCCURRED_AT, 'HH24:MI:SS') AS UTC, ACTOR_NAME, TARGET_PRM, OUTCOME,
       SUBSTR(CORRELATION_ID, 1, 8) AS CORRELATION, DBMS_LOB.SUBSTR(DETAILS, 90, 1) AS DETAILS
  FROM AUDIT_EVENT ORDER BY EVENT_ID;
EXIT
'@

Write-Host "`n=== Le compte de l'application ne peut ni modifier ni effacer une trace ==="
Executer-Sql 'portal_app' 'PORTAL_APP_PASSWORD' @'
UPDATE PORTAL.AUDIT_EVENT SET OUTCOME = 'SUCCES';
DELETE FROM PORTAL.AUDIT_EVENT;
EXIT
'@
