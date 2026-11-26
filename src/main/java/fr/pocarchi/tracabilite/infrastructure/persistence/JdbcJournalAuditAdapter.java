package fr.pocarchi.tracabilite.infrastructure.persistence;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.pocarchi.tracabilite.application.port.JournalAuditPort;
import fr.pocarchi.tracabilite.domain.EntreeJournal;
import fr.pocarchi.tracabilite.domain.ParametresMasques;
import fr.pocarchi.tracabilite.domain.Prm;
import fr.pocarchi.tracabilite.domain.ResultatAudit;
import fr.pocarchi.tracabilite.domain.TeleoperationJournalisee;
import tools.jackson.databind.json.JsonMapper;

@Repository
class JdbcJournalAuditAdapter implements JournalAuditPort {

    static final String ACTION_TELEOP_MONO = "TELEOP_MONO";

    private static final String INSERTION = """
        INSERT INTO PORTAL.AUDIT_EVENT
            (API_NAME, DESIGNATION, ACTOR_SUB, ACTOR_NAME, ACTION, TARGET_TYPE, TARGET_ID, TARGET_PRM,
             OUTCOME, CORRELATION_ID, DETAILS)
        VALUES
            (:api, :designation, :sub, :nom, :action, 'PRM', :prm, :prm,
             :resultat, :correlation, :details)
        """;

    private static final String LECTURE = """
        SELECT EVENT_ID, OCCURRED_AT, ACTOR_SUB, ACTOR_NAME, ACTION, TARGET_PRM, OUTCOME, CORRELATION_ID, DETAILS
          FROM PORTAL.AUDIT_EVENT
         WHERE DESIGNATION = :designation
           AND TARGET_PRM = :prm
         ORDER BY EVENT_ID
        """;

    private final JdbcClient jdbc;
    private final JsonMapper json;
    private final String apiName;

    JdbcJournalAuditAdapter(JdbcClient jdbc, JsonMapper json, @Value("${poc.api-name}") String apiName) {
        this.jdbc = jdbc;
        this.json = json;
        this.apiName = apiName;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrer(TeleoperationJournalisee evenement) {
        jdbc.sql(INSERTION)
            .param("api", apiName)
            .param("designation", evenement.auteur().designation())
            .param("sub", evenement.auteur().sub())
            .param("nom", evenement.auteur().nom())
            .param("action", ACTION_TELEOP_MONO)
            .param("prm", evenement.commande().prm().valeur())
            .param("resultat", evenement.resultat().name())
            .param("correlation", evenement.correlation())
            .param("details", details(evenement))
            .update();
    }

    @Override
    public List<EntreeJournal> lire(String designation, Prm prm) {
        return jdbc.sql(LECTURE)
            .param("designation", designation)
            .param("prm", prm.valeur())
            .query((rs, ligne) -> new EntreeJournal(
                rs.getLong("EVENT_ID"),
                rs.getObject("OCCURRED_AT", LocalDateTime.class).toInstant(ZoneOffset.UTC),
                rs.getString("ACTOR_SUB"),
                rs.getString("ACTOR_NAME"),
                rs.getString("ACTION"),
                rs.getString("TARGET_PRM"),
                ResultatAudit.valueOf(rs.getString("OUTCOME")),
                rs.getString("CORRELATION_ID"),
                rs.getString("DETAILS")))
            .list();
    }

    private String details(TeleoperationJournalisee evenement) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("service", evenement.commande().service());
        details.put("parametres", ParametresMasques.masquer(evenement.commande().parametres()));
        if (evenement.motif() != null) {
            details.put("motif", evenement.motif());
        }
        return json.writeValueAsString(details);
    }
}
