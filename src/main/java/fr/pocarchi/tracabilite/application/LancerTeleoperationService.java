package fr.pocarchi.tracabilite.application;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.pocarchi.tracabilite.application.port.PerimetrePort;
import fr.pocarchi.tracabilite.application.port.ReferentielCompteurPort;
import fr.pocarchi.tracabilite.application.port.SystemeCentralPort;
import fr.pocarchi.tracabilite.domain.Auteur;
import fr.pocarchi.tracabilite.domain.CommandeTeleoperation;
import fr.pocarchi.tracabilite.domain.PrmHorsPerimetreException;
import fr.pocarchi.tracabilite.domain.RefusSystemeCentralException;
import fr.pocarchi.tracabilite.domain.ResultatAudit;
import fr.pocarchi.tracabilite.domain.SystemeCentralSansReponseException;
import fr.pocarchi.tracabilite.domain.TeleoperationJournalisee;

/**
 * Lancement d'une téléopération unitaire. Le rôle est vérifié avant l'appel (contrôleur) ; ici : périmètre,
 * puis journal DEMANDE validé avant l'appel au système central, puis journal du résultat.
 * Pas de transaction englobante : l'appel SOAP ne peut pas être annulé, chaque trace est donc validée seule.
 */
@Service
public class LancerTeleoperationService {

    private final PerimetrePort perimetre;
    private final ReferentielCompteurPort referentiel;
    private final SystemeCentralPort systemeCentral;
    private final ApplicationEventPublisher evenements;

    public LancerTeleoperationService(PerimetrePort perimetre, ReferentielCompteurPort referentiel,
                                      SystemeCentralPort systemeCentral, ApplicationEventPublisher evenements) {
        this.perimetre = perimetre;
        this.referentiel = referentiel;
        this.systemeCentral = systemeCentral;
        this.evenements = evenements;
    }

    public String lancer(CommandeTeleoperation commande, Auteur auteur) {
        verifierPerimetre(commande, auteur);

        String correlation = UUID.randomUUID().toString();
        journaliser(correlation, auteur, commande, ResultatAudit.DEMANDE, null);
        try {
            String reference = systemeCentral.lancer(commande);
            journaliser(correlation, auteur, commande, ResultatAudit.SUCCES, "Référence " + reference);
            return reference;
        } catch (RefusSystemeCentralException e) {
            journaliser(correlation, auteur, commande, ResultatAudit.ECHEC, e.getMessage());
            throw e;
        } catch (SystemeCentralSansReponseException e) {
            journaliser(correlation, auteur, commande, ResultatAudit.INCONNU, e.getMessage());
            throw e;
        }
    }

    private void verifierPerimetre(CommandeTeleoperation commande, Auteur auteur) {
        String proprietaire = referentiel.proprietaire(commande.prm())
            .orElseThrow(() -> new PrmHorsPerimetreException(commande.prm()));
        if (!perimetre.proprietaires(auteur.designation()).contains(proprietaire)) {
            throw new PrmHorsPerimetreException(commande.prm());
        }
    }

    private void journaliser(String correlation, Auteur auteur, CommandeTeleoperation commande,
                             ResultatAudit resultat, String motif) {
        evenements.publishEvent(new TeleoperationJournalisee(correlation, auteur, commande, resultat, motif));
    }
}
