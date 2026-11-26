package fr.pocarchi.tracabilite.domain;

/** Le système central a reçu la demande et l'a refusée. */
public class RefusSystemeCentralException extends RuntimeException {

    public RefusSystemeCentralException(String motif) {
        super(motif);
    }
}
