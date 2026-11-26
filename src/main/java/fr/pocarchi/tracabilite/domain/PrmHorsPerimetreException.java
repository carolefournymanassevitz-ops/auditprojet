package fr.pocarchi.tracabilite.domain;

/** PRM inconnu ou hors du périmètre : traité comme inexistant pour ne pas révéler sa présence (SEC-03). */
public class PrmHorsPerimetreException extends RuntimeException {

    public PrmHorsPerimetreException(Prm prm) {
        super("Compteur introuvable : " + prm.valeur());
    }
}
