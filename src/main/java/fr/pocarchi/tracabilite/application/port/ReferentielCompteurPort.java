package fr.pocarchi.tracabilite.application.port;

import java.util.Optional;

import fr.pocarchi.tracabilite.domain.Prm;

/** Lecture du référentiel compteur du système central (read model). */
public interface ReferentielCompteurPort {

    Optional<String> proprietaire(Prm prm);
}
