package fr.pocarchi.tracabilite.domain;

/**
 * Étape d'une action dans le journal. INCONNU : le système central n'a pas répondu, la demande a pu être reçue ;
 * c'est une trace à part entière, jamais une absence de trace.
 */
public enum ResultatAudit {
    DEMANDE,
    SUCCES,
    ECHEC,
    INCONNU
}
