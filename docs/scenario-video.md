# Scénario de la vidéo (≈ 5 minutes)

Préparation, hors caméra : `.env` rempli, `docker compose down` puis `docker compose up -d` pour un journal vide,
application démarrée, deux consoles PowerShell ouvertes, l'IDE ouvert sur le projet.

| Temps | À l'écran | Message |
|---|---|---|
| 0:00 – 0:30 | README, sections « En bref » et « Le besoin » | Un seul aspect : la traçabilité sécurisée des actions sensibles, exigence de sécurité la plus structurante du cahier des charges ; le cas de la coupure à distance |
| 0:30 – 1:15 | README, section « Structure des données », puis `V1__audit_event.sql`, `V2__droits_portal_app.sql`, `01-schemas.sh` | Schéma versionné par Flyway ; deux comptes : `PORTAL` migre, `PORTAL_APP` ne peut qu'insérer et lire ; choix compatibles avec Oracle 19c (IDENTITY, `IS JSON`, UTC) |
| 1:15 – 2:00 | README, section « Comment ça marche » (diagramme de séquence), puis `LancerTeleoperationService`, `TeleoperationController`, `AuteurJeton` | Rôle exigé, périmètre, auteur tiré du jeton ; demande écrite avant l'appel, résultat après, aucune nouvelle tentative |
| 2:00 – 3:30 | Console : `.\demo.ps1` (README, section « Lancer la démonstration ») | Commenter les 9 cas : quatre refus sans trace, puis succès, refus et absence de réponse, chacun avec sa paire d'entrées ; `cleAdmin` masqué ; journal invisible pour une autre ELD |
| 3:30 – 4:00 | Fin de `demo.ps1` | Contenu réel de `AUDIT_EVENT`, puis `UPDATE` et `DELETE` refusés par Oracle : l'ajout seul est garanti par la base |
| 4:00 – 4:40 | Console : `mvn verify` (ou rapport déjà produit), README section « Lancer les tests » | 25 tests sur Oracle en conteneur ; montrer `ArchitectureTest` (README, section « Architecture du code ») : un point d'entrée sans rôle ferait échouer le build |
| 4:40 – 5:00 | README, section « Limites assumées » | Ce qui n'est pas démontré, et pourquoi |
