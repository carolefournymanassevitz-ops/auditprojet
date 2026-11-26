package fr.pocarchi.tracabilite.infrastructure.persistence;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import fr.pocarchi.tracabilite.application.port.ReferentielCompteurPort;
import fr.pocarchi.tracabilite.domain.Prm;

/** Lit le référentiel du système central simulé ; le portail n'y écrit jamais. */
@Repository
class JdbcReferentielCompteurAdapter implements ReferentielCompteurPort {

    private final JdbcClient jdbc;

    JdbcReferentielCompteurAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> proprietaire(Prm prm) {
        return jdbc.sql("SELECT OWNER FROM CENTRAL_SIM.METER WHERE PRM = :prm")
            .param("prm", prm.valeur())
            .query(String.class)
            .optional();
    }
}
