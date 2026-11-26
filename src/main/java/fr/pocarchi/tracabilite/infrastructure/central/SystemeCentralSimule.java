package fr.pocarchi.tracabilite.infrastructure.central;

import java.util.UUID;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;

import fr.pocarchi.tracabilite.application.port.SystemeCentralPort;
import fr.pocarchi.tracabilite.domain.CommandeTeleoperation;
import fr.pocarchi.tracabilite.domain.RefusSystemeCentralException;
import fr.pocarchi.tracabilite.domain.SystemeCentralSansReponseException;

/**
 * Remplace le service SOAP du système central, hors du périmètre de la PoC. Comportement fixé par le dernier
 * chiffre du PRM : 2 = refus, 9 = pas de réponse, sinon acceptation.
 * Limite de concurrence, mais aucune nouvelle tentative : une commande rejouée partirait deux fois
 * (ST-14 : appels au système central bornés par un délai d'attente ; aucune commande rejouée automatiquement).
 */
@Component
class SystemeCentralSimule implements SystemeCentralPort {

    @Override
    @ConcurrencyLimit(5)
    public String lancer(CommandeTeleoperation commande) {
        String prm = commande.prm().valeur();
        if (prm.endsWith("2")) {
            throw new RefusSystemeCentralException("Refus du système central simulé pour le service " + commande.service());
        }
        if (prm.endsWith("9")) {
            throw new SystemeCentralSansReponseException("Aucune réponse du système central dans le délai");
        }
        return "SIM-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
