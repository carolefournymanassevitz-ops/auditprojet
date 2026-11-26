package fr.pocarchi.tracabilite.domain;

/**
 * Identité de l'auteur d'une action, toujours issue du jeton d'authentification
 * (ST-04 : auteur lu dans le jeton, jamais fourni par le client).
 */
public record Auteur(String sub, String nom, String designation) {

    public Auteur {
        if (sub == null || sub.isBlank() || designation == null || designation.isBlank()) {
            throw new IllegalArgumentException("Auteur incomplet : sub et désignation obligatoires");
        }
    }
}
