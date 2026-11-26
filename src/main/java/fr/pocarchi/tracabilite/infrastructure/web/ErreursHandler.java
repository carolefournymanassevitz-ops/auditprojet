package fr.pocarchi.tracabilite.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.pocarchi.tracabilite.domain.PrmHorsPerimetreException;
import fr.pocarchi.tracabilite.domain.RefusSystemeCentralException;
import fr.pocarchi.tracabilite.domain.SystemeCentralSansReponseException;

/** Erreurs métier en ProblemDetail. Les refus d'accès restent gérés par Spring Security (401, 403). */
@RestControllerAdvice
class ErreursHandler {

    @ExceptionHandler(PrmHorsPerimetreException.class)
    ProblemDetail horsPerimetre(PrmHorsPerimetreException e) {
        return probleme(HttpStatus.NOT_FOUND, "Compteur introuvable", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalide(IllegalArgumentException e) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête invalide", e.getMessage());
    }

    @ExceptionHandler(RefusSystemeCentralException.class)
    ProblemDetail refus(RefusSystemeCentralException e) {
        return probleme(HttpStatus.UNPROCESSABLE_CONTENT, "Demande refusée par le système central", e.getMessage());
    }

    @ExceptionHandler(SystemeCentralSansReponseException.class)
    ProblemDetail sansReponse(SystemeCentralSansReponseException e) {
        return probleme(HttpStatus.GATEWAY_TIMEOUT, "Résultat inconnu",
            "La demande a pu être reçue : vérifier le suivi avant toute relance. " + e.getMessage());
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setTitle(titre);
        return probleme;
    }
}
