package fr.pocarchi.tracabilite.domain;

import java.time.Instant;

/** Ligne du journal telle qu'elle est relue : jamais modifiée après écriture. */
public record EntreeJournal(
    long id,
    Instant date,
    String auteurSub,
    String auteurNom,
    String action,
    String prm,
    ResultatAudit resultat,
    String correlation,
    String details
) {
}
