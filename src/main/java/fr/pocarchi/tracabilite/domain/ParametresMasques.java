package fr.pocarchi.tracabilite.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Remplace la valeur des paramètres sensibles avant toute journalisation
 * (ST-05 : aucune clé de compteur renvoyée au navigateur ni écrite dans un journal).
 */
public final class ParametresMasques {

    public static final String MASQUE = "***";

    private static final Pattern SENSIBLE = Pattern.compile("(?i).*(cle|clé|key|secret|password|mdp).*");

    private ParametresMasques() {
    }

    public static Map<String, String> masquer(Map<String, String> parametres) {
        Map<String, String> resultat = new LinkedHashMap<>();
        new TreeMap<>(parametres).forEach((nom, valeur) ->
            resultat.put(nom, SENSIBLE.matcher(nom).matches() ? MASQUE : valeur));
        return resultat;
    }
}
