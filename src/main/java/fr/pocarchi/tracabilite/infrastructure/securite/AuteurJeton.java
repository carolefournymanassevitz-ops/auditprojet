package fr.pocarchi.tracabilite.infrastructure.securite;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import fr.pocarchi.tracabilite.domain.Auteur;

/** Construit l'auteur à partir du jeton seul : `sub`, `preferred_username`, et la désignation portée par le groupe. */
public final class AuteurJeton {

    static final String CLAIM_DESIGNATION = "designation";

    private AuteurJeton() {
    }

    public static Auteur depuis(Jwt jwt) {
        String designation = designation(jwt);
        if (designation == null) {
            throw new AccessDeniedException("Jeton sans désignation : aucune donnée accessible");
        }
        return new Auteur(jwt.getSubject(), jwt.getClaimAsString("preferred_username"), designation);
    }

    private static String designation(Jwt jwt) {
        Object valeur = jwt.getClaim(CLAIM_DESIGNATION);
        if (valeur instanceof String texte && !texte.isBlank()) {
            return texte;
        }
        // Un utilisateur rattaché à plusieurs désignations n'a pas de périmètre défini : refus plutôt que choix arbitraire.
        if (valeur instanceof List<?> liste && liste.size() == 1 && liste.getFirst() instanceof String texte) {
            return texte;
        }
        return null;
    }
}
