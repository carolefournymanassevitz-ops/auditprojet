package fr.pocarchi.tracabilite.application.port;

import fr.pocarchi.tracabilite.domain.CommandeTeleoperation;

/** Couche anti-corruption vers le système central : le reste de l'application ne voit jamais son contrat SOAP. */
public interface SystemeCentralPort {

    /**
     * @return la référence de la demande créée par le système central
     * @throws fr.pocarchi.tracabilite.domain.RefusSystemeCentralException si la demande est refusée
     * @throws fr.pocarchi.tracabilite.domain.SystemeCentralSansReponseException sans réponse dans le délai
     */
    String lancer(CommandeTeleoperation commande);
}
