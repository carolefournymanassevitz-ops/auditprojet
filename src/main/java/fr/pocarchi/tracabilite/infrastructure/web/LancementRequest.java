package fr.pocarchi.tracabilite.infrastructure.web;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * Aucun champ auteur : l'auteur vient du jeton, un champ en trop est refusé
 * (ST-04 : auteur lu dans le jeton, jamais fourni par le client).
 */
record LancementRequest(@NotBlank String prm, @NotBlank String service, Map<String, String> parametres) {
}
