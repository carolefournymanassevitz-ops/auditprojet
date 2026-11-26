package fr.pocarchi.tracabilite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.pocarchi.tracabilite.application.port.SystemeCentralPort;
import fr.pocarchi.tracabilite.support.Jetons;
import fr.pocarchi.tracabilite.support.Journal;
import fr.pocarchi.tracabilite.support.Journal.Ligne;
import fr.pocarchi.tracabilite.support.OracleConteneurTest;

/**
 * Chaque lancement autorisé laisse une demande puis un résultat, liés par une corrélation, quel que soit le
 * comportement du système central ; la commande n'est jamais envoyée deux fois.
 * ST-09 : journal d'audit en ajout seul : qui, quoi, quand (UTC), sur quoi, résultat.
 * ST-14 : appels au système central bornés par un délai d'attente ; aucune commande rejouée automatiquement.
 * ST-05 : aucune clé de compteur renvoyée au navigateur ni écrite dans un journal.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JournalTeleoperationIntegrationTest extends OracleConteneurTest {

    private static final String PRM_ACCEPTE = "30001000000001";
    private static final String PRM_REFUSE = "30001000000002";
    private static final String PRM_SANS_REPONSE = "30001000000009";
    private static final String SECRET = "VALEUR-SECRETE-123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcClient jdbc;

    @MockitoSpyBean
    private SystemeCentralPort systemeCentral;

    @Test
    void un_succes_laisse_une_demande_puis_un_succes_au_nom_du_jeton() throws Exception {
        mvc.perform(lancement(PRM_ACCEPTE, "{\"cleAdmin\":\"" + SECRET + "\",\"periode\":\"J-1\"}").with(Jetons.operateurEldA()))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.reference").exists());

        List<Ligne> paire = derniereCorrelation(PRM_ACCEPTE);
        assertThat(paire).extracting(Ligne::resultat).containsExactly("DEMANDE", "SUCCES");
        assertThat(paire).extracting(Ligne::auteurSub).containsOnly(Jetons.OPERATEUR_A);
        verify(systemeCentral, times(1)).lancer(any());
    }

    @Test
    void aucune_valeur_de_cle_n_est_journalisee() throws Exception {
        mvc.perform(lancement(PRM_ACCEPTE, "{\"cleAdmin\":\"" + SECRET + "\"}").with(Jetons.operateurEldA()))
            .andExpect(status().isAccepted());

        assertThat(derniereCorrelation(PRM_ACCEPTE)).extracting(Ligne::details)
            .allSatisfy(details -> assertThat(details).doesNotContain(SECRET).contains("\"cleAdmin\":\"***\""));
    }

    @Test
    void la_demande_est_ecrite_avant_l_appel_au_systeme_central() throws Exception {
        long demandesAvant = nombreDeDemandes(PRM_ACCEPTE);
        AtomicLong demandesAuMomentDeLAppel = new AtomicLong(-1);
        doAnswer(appel -> {
            demandesAuMomentDeLAppel.set(nombreDeDemandes(PRM_ACCEPTE));
            return appel.callRealMethod();
        }).when(systemeCentral).lancer(any());

        mvc.perform(lancement(PRM_ACCEPTE, "{}").with(Jetons.operateurEldA())).andExpect(status().isAccepted());

        assertThat(demandesAuMomentDeLAppel.get()).isEqualTo(demandesAvant + 1);
    }

    @Test
    void un_refus_laisse_une_demande_puis_un_echec() throws Exception {
        mvc.perform(lancement(PRM_REFUSE, "{}").with(Jetons.operateurEldA()))
            .andExpect(status().isUnprocessableContent());

        assertThat(derniereCorrelation(PRM_REFUSE)).extracting(Ligne::resultat).containsExactly("DEMANDE", "ECHEC");
    }

    @Test
    void sans_reponse_le_resultat_est_inconnu_et_la_commande_n_est_pas_rejouee() throws Exception {
        mvc.perform(lancement(PRM_SANS_REPONSE, "{}").with(Jetons.operateurEldA()))
            .andExpect(status().isGatewayTimeout());

        assertThat(derniereCorrelation(PRM_SANS_REPONSE)).extracting(Ligne::resultat).containsExactly("DEMANDE", "INCONNU");
        verify(systemeCentral, times(1)).lancer(any());
    }

    @Test
    void le_journal_ne_montre_que_les_entrees_de_sa_propre_eld() throws Exception {
        mvc.perform(lancement(PRM_ACCEPTE, "{}").with(Jetons.operateurEldA())).andExpect(status().isAccepted());

        mvc.perform(get("/teleoperations/journal").param("prm", PRM_ACCEPTE).with(Jetons.lecteurEldA()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].auteur").value("operateur.a"));
        mvc.perform(get("/teleoperations/journal").param("prm", PRM_ACCEPTE).with(Jetons.operateurEldB()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    private List<Ligne> derniereCorrelation(String prm) {
        List<Ligne> lignes = Journal.lignes(jdbc, prm);
        String correlation = lignes.getLast().correlation();
        return lignes.stream().filter(ligne -> ligne.correlation().equals(correlation)).toList();
    }

    private long nombreDeDemandes(String prm) {
        return Journal.lignes(jdbc, prm).stream().filter(ligne -> ligne.resultat().equals("DEMANDE")).count();
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder lancement(String prm, String parametres) {
        return post("/teleoperations/mono")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"prm":"%s","service":"LECTURE_DONNEES","parametres":%s}""".formatted(prm, parametres));
    }
}
