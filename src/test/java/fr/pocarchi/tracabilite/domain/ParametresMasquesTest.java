package fr.pocarchi.tracabilite.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ParametresMasquesTest {

    @Test
    void masque_les_parametres_sensibles_et_garde_les_autres() {
        Map<String, String> masques = ParametresMasques.masquer(Map.of(
            "cleAdmin", "A1", "CleClient", "B2", "apiKey", "C3", "password", "D4", "periode", "J-1"));

        assertThat(masques)
            .containsEntry("cleAdmin", ParametresMasques.MASQUE)
            .containsEntry("CleClient", ParametresMasques.MASQUE)
            .containsEntry("apiKey", ParametresMasques.MASQUE)
            .containsEntry("password", ParametresMasques.MASQUE)
            .containsEntry("periode", "J-1");
    }

    @Test
    void des_parametres_absents_donnent_une_liste_vide() {
        assertThat(ParametresMasques.masquer(Map.of())).isEmpty();
    }
}
