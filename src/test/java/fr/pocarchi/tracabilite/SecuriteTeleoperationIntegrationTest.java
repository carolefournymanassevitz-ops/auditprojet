package fr.pocarchi.tracabilite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fr.pocarchi.tracabilite.support.Jetons;
import fr.pocarchi.tracabilite.support.Journal;
import fr.pocarchi.tracabilite.support.OracleConteneurTest;

/**
 * Contrôles serveur sur la vraie configuration de sécurité. Chaque refus est vérifié
 * deux fois : par le code HTTP, et par l'absence de toute écriture au journal.
 * ST-02 : toute écriture exige un rôle vérifié côté serveur, refus par défaut.
 * ST-03 : périmètre calculé par le serveur à chaque requête ; une ressource hors périmètre est traitée comme
 * inexistante (404).
 * ST-04 : auteur lu dans le jeton, jamais fourni par le client.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecuriteTeleoperationIntegrationTest extends OracleConteneurTest {

    private static final String PRM_ELD_A = "30001000000001";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void sans_jeton_la_requete_est_refusee() throws Exception {
        verifierRefusSansTrace(lancement(corps(PRM_ELD_A)), 401);
    }

    @Test
    void un_compte_lecture_seule_ne_peut_pas_lancer() throws Exception {
        verifierRefusSansTrace(lancement(corps(PRM_ELD_A)).with(Jetons.lecteurEldA()), 403);
    }

    @Test
    void un_jeton_sans_designation_ne_donne_acces_a_rien() throws Exception {
        verifierRefusSansTrace(lancement(corps(PRM_ELD_A)).with(Jetons.operateurSansDesignation()), 403);
    }

    @Test
    void un_compteur_d_une_autre_eld_est_traite_comme_inexistant() throws Exception {
        verifierRefusSansTrace(lancement(corps(PRM_ELD_A)).with(Jetons.operateurEldB()), 404);
    }

    @Test
    void un_compteur_inconnu_renvoie_404() throws Exception {
        verifierRefusSansTrace(lancement(corps("30009999999999")).with(Jetons.operateurEldA()), 404);
    }

    @Test
    void un_auteur_fourni_dans_le_corps_est_refuse() throws Exception {
        String corpsAvecAuteur = """
            {"prm":"%s","service":"LECTURE_DONNEES","parametres":{},"auteur":"quelqu.un.d.autre"}""".formatted(PRM_ELD_A);
        verifierRefusSansTrace(lancement(corpsAvecAuteur).with(Jetons.operateurEldA()), 400);
    }

    @Test
    void un_prm_mal_forme_est_refuse() throws Exception {
        verifierRefusSansTrace(lancement(corps("PRM-INVALIDE!")).with(Jetons.operateurEldA()), 400);
    }

    @Test
    void un_compte_lecture_seule_peut_consulter_le_journal_de_son_eld() throws Exception {
        mvc.perform(get("/teleoperations/journal").param("prm", PRM_ELD_A).with(Jetons.lecteurEldA()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    private void verifierRefusSansTrace(MockHttpServletRequestBuilder requete, int statutAttendu) throws Exception {
        int avant = Journal.nombre(jdbc);
        mvc.perform(requete).andExpect(status().is(statutAttendu));
        assertThat(Journal.nombre(jdbc)).as("aucune écriture au journal pour une requête refusée").isEqualTo(avant);
    }

    private static MockHttpServletRequestBuilder lancement(String corps) {
        return post("/teleoperations/mono").contentType(MediaType.APPLICATION_JSON).content(corps);
    }

    private static String corps(String prm) {
        return """
            {"prm":"%s","service":"LECTURE_DONNEES","parametres":{}}""".formatted(prm);
    }
}
