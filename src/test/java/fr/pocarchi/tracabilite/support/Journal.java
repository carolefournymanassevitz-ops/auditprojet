package fr.pocarchi.tracabilite.support;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Lecture directe du journal en base, pour vérifier ce qui a réellement été écrit. */
public final class Journal {

    public record Ligne(String resultat, String correlation, String auteurSub, String details) {
    }

    private Journal() {
    }

    public static List<Ligne> lignes(JdbcClient jdbc, String prm) {
        return jdbc.sql("""
                SELECT OUTCOME, CORRELATION_ID, ACTOR_SUB, DETAILS
                  FROM PORTAL.AUDIT_EVENT
                 WHERE TARGET_PRM = :prm
                 ORDER BY EVENT_ID""")
            .param("prm", prm)
            .query((rs, i) -> new Ligne(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)))
            .list();
    }

    public static int nombre(JdbcClient jdbc) {
        return jdbc.sql("SELECT COUNT(*) FROM PORTAL.AUDIT_EVENT").query(Integer.class).single();
    }
}
