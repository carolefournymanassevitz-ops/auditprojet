package fr.pocarchi.tracabilite.infrastructure.journal;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.pocarchi.tracabilite.application.port.JournalAuditPort;
import fr.pocarchi.tracabilite.domain.TeleoperationJournalisee;

/**
 * Consomme les événements de domaine et les écrit au journal. Écouteur synchrone : si l'écriture échoue,
 * l'exception remonte au service et la commande n'est pas envoyée (jamais d'action sans trace).
 */
@Component
class JournalAuditListener {

    private final JournalAuditPort journal;

    JournalAuditListener(JournalAuditPort journal) {
        this.journal = journal;
    }

    @EventListener
    void enregistrer(TeleoperationJournalisee evenement) {
        journal.enregistrer(evenement);
    }
}
