package fr.pocarchi.tracabilite.application.port;

import java.util.List;

import fr.pocarchi.tracabilite.domain.EntreeJournal;
import fr.pocarchi.tracabilite.domain.Prm;
import fr.pocarchi.tracabilite.domain.TeleoperationJournalisee;

public interface JournalAuditPort {

    /** Écrit et valide immédiatement l'entrée, indépendamment de toute transaction en cours. */
    void enregistrer(TeleoperationJournalisee evenement);

    List<EntreeJournal> lire(String designation, Prm prm);
}
