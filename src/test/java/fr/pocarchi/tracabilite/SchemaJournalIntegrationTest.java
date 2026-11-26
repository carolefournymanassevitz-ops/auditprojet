package fr.pocarchi.tracabilite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import fr.pocarchi.tracabilite.support.OracleConteneurTest;

/** Le schéma PORTAL est versionné par Flyway, et le journal est en ajout seul par construction des droits Oracle. */
@SpringBootTest
class SchemaJournalIntegrationTest extends OracleConteneurTest {

    private static final String INSERTION = """
        INSERT INTO PORTAL.AUDIT_EVENT (API_NAME, DESIGNATION, ACTOR_SUB, ACTION, TARGET_TYPE, TARGET_ID, OUTCOME, CORRELATION_ID, DETAILS)
        VALUES ('test', 'ELD_A', 'sub-test', 'TEST', 'PRM', 'X', 'DEMANDE', 'corr-schema', :details)""";

    @Autowired
    private JdbcClient jdbc;

    @Test
    void flyway_a_applique_les_deux_migrations_avec_le_compte_proprietaire() throws Exception {
        List<String> versions = new ArrayList<>();
        try (Connection connexion = connexionProprietaire();
             ResultSet rs = connexion.createStatement().executeQuery(
                 "SELECT \"version\" FROM \"flyway_schema_history\" WHERE \"success\" = 1 ORDER BY \"installed_rank\"")) {
            while (rs.next()) {
                versions.add(rs.getString(1));
            }
        }
        assertThat(versions).containsExactly("1", "2");
    }

    @Test
    void le_compte_applicatif_peut_ecrire_et_relire() {
        jdbc.sql(INSERTION).param("details", "{\"ok\":true}").update();

        Integer nombre = jdbc.sql("SELECT COUNT(*) FROM PORTAL.AUDIT_EVENT WHERE CORRELATION_ID = 'corr-schema'")
            .query(Integer.class).single();
        assertThat(nombre).isPositive();
    }

    // Même refus, code différent selon la version : ORA-01031 en 19c (production), ORA-41900 en 23 (image de test).
    private static final String PRIVILEGE_MANQUANT = "(?s).*ORA-(01031|41900).*";

    @Test
    void le_compte_applicatif_ne_peut_pas_modifier_une_entree() {
        assertThatThrownBy(() -> jdbc.sql("UPDATE PORTAL.AUDIT_EVENT SET OUTCOME = 'SUCCES'").update())
            .isInstanceOf(DataAccessException.class)
            .rootCause().hasMessageMatching(PRIVILEGE_MANQUANT);
    }

    @Test
    void le_compte_applicatif_ne_peut_pas_supprimer_une_entree() {
        assertThatThrownBy(() -> jdbc.sql("DELETE FROM PORTAL.AUDIT_EVENT").update())
            .isInstanceOf(DataAccessException.class)
            .rootCause().hasMessageMatching(PRIVILEGE_MANQUANT);
    }

    @Test
    void les_details_doivent_etre_du_json() {
        assertThatThrownBy(() -> jdbc.sql(INSERTION).param("details", "pas du json").update())
            .isInstanceOf(DataAccessException.class)
            .rootCause().hasMessageContaining("CK_AUDIT_EVENT_DETAILS");
    }
}
