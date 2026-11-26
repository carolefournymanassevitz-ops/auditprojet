package fr.pocarchi.tracabilite.domain;

/** Pas de réponse dans le délai : la demande a pu être reçue, d'où l'interdiction de la rejouer automatiquement. */
public class SystemeCentralSansReponseException extends RuntimeException {

    public SystemeCentralSansReponseException(String motif) {
        super(motif);
    }
}
