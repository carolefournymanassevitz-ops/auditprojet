package fr.pocarchi.tracabilite.domain;

import java.util.regex.Pattern;

/** Point de livraison : 1 à 20 caractères alphanumériques. */
public record Prm(String valeur) {

    private static final Pattern FORMAT = Pattern.compile("[0-9A-Za-z]{1,20}");

    public Prm {
        if (valeur == null || !FORMAT.matcher(valeur).matches()) {
            throw new IllegalArgumentException("PRM invalide : 1 à 20 caractères alphanumériques attendus");
        }
    }
}
