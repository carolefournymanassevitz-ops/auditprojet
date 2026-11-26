package fr.pocarchi.tracabilite.infrastructure.central;

import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

/** Active @ConcurrencyLimit (résilience native de Spring Framework 7, sans bibliothèque externe). */
@Configuration
@EnableResilientMethods
class ResilienceConfig {
}
