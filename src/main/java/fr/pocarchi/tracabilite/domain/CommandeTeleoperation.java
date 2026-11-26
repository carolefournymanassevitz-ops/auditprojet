package fr.pocarchi.tracabilite.domain;

import java.util.Map;

/** Demande de téléopération unitaire : un PRM, un service, des paramètres. */
public record CommandeTeleoperation(Prm prm, String service, Map<String, String> parametres) {

    public CommandeTeleoperation {
        if (prm == null) {
            throw new IllegalArgumentException("PRM obligatoire");
        }
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("Service obligatoire");
        }
        parametres = parametres == null ? Map.of() : Map.copyOf(parametres);
    }
}
