package fr.pocarchi.tracabilite.application;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.pocarchi.tracabilite.application.port.JournalAuditPort;
import fr.pocarchi.tracabilite.domain.Auteur;
import fr.pocarchi.tracabilite.domain.EntreeJournal;
import fr.pocarchi.tracabilite.domain.Prm;

/**
 * Lecture du journal, toujours limitée à la désignation du lecteur (RG-10 : un utilisateur ne voit et n'agit que sur
 * les équipements dont son ELD est propriétaire ou gestionnaire).
 */
@Service
public class ConsulterJournalService {

    private final JournalAuditPort journal;

    public ConsulterJournalService(JournalAuditPort journal) {
        this.journal = journal;
    }

    public List<EntreeJournal> consulter(Prm prm, Auteur lecteur) {
        return journal.lire(lecteur.designation(), prm);
    }
}
