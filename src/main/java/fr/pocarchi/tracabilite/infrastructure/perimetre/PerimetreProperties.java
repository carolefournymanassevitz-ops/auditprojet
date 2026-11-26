package fr.pocarchi.tracabilite.infrastructure.perimetre;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "poc")
record PerimetreProperties(Map<String, Set<String>> perimetre) {

    PerimetreProperties {
        perimetre = perimetre == null ? Map.of() : Map.copyOf(perimetre);
    }
}
