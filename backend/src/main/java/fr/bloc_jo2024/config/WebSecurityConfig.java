package fr.bloc_jo2024.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AdminSessionFilter adminSessionFilter;

    /**
     * Crée et configure un PasswordEncoder pour encoder les mots de passe.
     * @return BCryptPasswordEncoder bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Constructeur pour injecter les dépendances (JwtAuthenticationFilter et AdminSessionFilter).
     * @param jwtAuthFilter    Filtre pour l'authentification basée sur JWT.
     * @param adminSessionFilter Filtre pour la gestion de la session admin.
     */
    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AdminSessionFilter adminSessionFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.adminSessionFilter = adminSessionFilter;
    }

    /**
     * Définit la chaîne de filtres de sécurité pour les requêtes HTTP.
     * @param http HttpSecurity builder pour configurer la sécurité.
     * @return SecurityFilterChain configurée.
     * @throws Exception En cas d'erreur lors de la configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                // Désactive la protection CSRF (Cross-Site Request Forgery) pour les API REST.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure la gestion de session pour les utilisateurs standard (JWT).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Définit les règles d'autorisation pour les requêtes HTTP.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/api/admin/auth/**").permitAll() // Permettre l'accès aux routes d'authentification admin
                        .requestMatchers("/api/admin/**").authenticated() // Les autres routes admin nécessitent une authentification (via le filtre de session)
                        .anyRequest().authenticated()
                )

                // Ajout du filtre JWT avant le filtre UsernamePasswordAuthenticationFilter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Ajout le filtre de session admin avant le BasicAuthenticationFilter (qui gère l'authentification basique HTTP).
                .addFilterBefore(adminSessionFilter, BasicAuthenticationFilter.class)
                .build();
    }

    /**
     * Crée et configure l'AuthenticationManager, utilisé pour authentifier les utilisateurs.
     * @param config Configuration d'authentification.
     * @return AuthenticationManager bean.
     * @throws Exception En cas d'erreur lors de la configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
