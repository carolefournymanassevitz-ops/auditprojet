package fr.pocarchi.tracabilite.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Une seule base Oracle Free pour toute la suite (démarrée une fois, arrêtée par Testcontainers en fin de JVM),
 * initialisée par le même script que docker-compose. Mots de passe tirés au hasard : rien n'est versionné.
 * Oracle Free n'existe qu'en version 23 : les migrations s'en tiennent volontairement à la syntaxe 19c.
 * Étiquette versionnée, identique à celle de docker-compose.yml : tests et démonstration utilisent la même image.
 */
public abstract class OracleConteneurTest {

    private static final String MDP_PORTAL = motDePasse();
    private static final String MDP_PORTAL_APP = motDePasse();

    protected static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23.26.4-slim-faststart")
        .withEnv("PORTAL_PASSWORD", MDP_PORTAL)
        .withEnv("PORTAL_APP_PASSWORD", MDP_PORTAL_APP)
        .withCopyFileToContainer(
            MountableFile.forHostPath("docker/oracle/init/01-schemas.sh", 0755),
            "/container-entrypoint-initdb.d/01-schemas.sh");

    static {
        ORACLE.start();
    }

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registre) {
        registre.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registre.add("spring.datasource.password", () -> MDP_PORTAL_APP);
        registre.add("spring.flyway.password", () -> MDP_PORTAL);
    }

    /** Connexion avec le compte propriétaire, pour inspecter ce que l'application ne peut pas voir. */
    protected static Connection connexionProprietaire() throws SQLException {
        return DriverManager.getConnection(ORACLE.getJdbcUrl(), "portal", MDP_PORTAL);
    }

    private static String motDePasse() {
        return "Poc" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
