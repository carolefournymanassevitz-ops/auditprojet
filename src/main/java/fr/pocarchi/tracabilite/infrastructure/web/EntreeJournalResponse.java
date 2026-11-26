package fr.pocarchi.tracabilite.infrastructure.web;

import java.time.Instant;

import fr.pocarchi.tracabilite.domain.EntreeJournal;

record EntreeJournalResponse(long id, Instant date, String auteur, String action, String prm, String resultat,
                             String correlation, String details) {

    static EntreeJournalResponse depuis(EntreeJournal entree) {
        return new EntreeJournalResponse(entree.id(), entree.date(), entree.auteurNom(), entree.action(), entree.prm(),
            entree.resultat().name(), entree.correlation(), entree.details());
    }
}
