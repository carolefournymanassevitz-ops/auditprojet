package fr.pocarchi.tracabilite.infrastructure.perimetre;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import fr.pocarchi.tracabilite.application.port.PerimetrePort;

/**
 * Périmètre lu dans la configuration. Dans l'architecture cible, c'est le service de configuration qui le sert (avec
 * cache côté appelant) ; la PoC ne démontre pas cet appel, seulement que chaque requête passe par le périmètre.
 */
@Component
class ConfigurationPerimetreAdapter implements PerimetrePort {

    private final Map<String, Set<String>> perimetres;

    ConfigurationPerimetreAdapter(PerimetreProperties proprietes) {
        this.perimetres = proprietes.perimetre();
    }

    @Override
    public Set<String> proprietaires(String designation) {
        return perimetres.getOrDefault(designation, Set.of());
    }
}
