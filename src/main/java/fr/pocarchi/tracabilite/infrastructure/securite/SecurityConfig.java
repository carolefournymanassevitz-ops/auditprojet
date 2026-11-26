package fr.pocarchi.tracabilite.infrastructure.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sans session, jeton Keycloak obligatoire. Les rôles sont vérifiés méthode par méthode (@PreAuthorize) ;
 * le test d'architecture refuse toute méthode de contrôleur qui n'en déclare pas.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requetes -> requetes.anyRequest().authenticated())
            .oauth2ResourceServer(serveur -> serveur.jwt(jwt -> jwt.jwtAuthenticationConverter(convertisseurJwt())));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter convertisseurJwt() {
        JwtAuthenticationConverter convertisseur = new JwtAuthenticationConverter();
        convertisseur.setJwtGrantedAuthoritiesConverter(new RolesKeycloakConverter());
        return convertisseur;
    }
}
