package fr.pocarchi.tracabilite.domain;

/** Événement de domaine : une étape d'une téléopération, à journaliser. La corrélation relie demande et résultat. */
public record TeleoperationJournalisee(
    String correlation,
    Auteur auteur,
    CommandeTeleoperation commande,
    ResultatAudit resultat,
    String motif
) {
}
