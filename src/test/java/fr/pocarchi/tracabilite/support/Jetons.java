package fr.pocarchi.tracabilite.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import fr.pocarchi.tracabilite.infrastructure.securite.RolesKeycloakConverter;

/** Jetons de test au format Keycloak, convertis par le vrai convertisseur de rôles de l'application. */
public final class Jetons {

    public static final String OPERATEUR_A = "sub-operateur-a";
    public static final String LECTEUR_A = "sub-lecteur-a";
    public static final String OPERATEUR_B = "sub-operateur-b";

    private Jetons() {
    }

    public static RequestPostProcessor operateurEldA() {
        return jeton(OPERATEUR_A, "operateur.a", "ELD_A", "OPERATEUR");
    }

    public static RequestPostProcessor lecteurEldA() {
        return jeton(LECTEUR_A, "lecteur.a", "ELD_A", "LECTURE_SEULE");
    }

    public static RequestPostProcessor operateurEldB() {
        return jeton(OPERATEUR_B, "operateur.b", "ELD_B", "OPERATEUR");
    }

    public static RequestPostProcessor operateurSansDesignation() {
        return jeton("sub-sans-eld", "sans.eld", null, "OPERATEUR");
    }

    private static RequestPostProcessor jeton(String sub, String nom, String designation, String... roles) {
        return jwt()
            .jwt(builder -> {
                builder.subject(sub)
                    .claim("preferred_username", nom)
                    .claim("realm_access", Map.of("roles", List.of(roles)));
                if (designation != null) {
                    builder.claim("designation", List.of(designation));
                }
            })
            .authorities(new RolesKeycloakConverter());
    }
}
