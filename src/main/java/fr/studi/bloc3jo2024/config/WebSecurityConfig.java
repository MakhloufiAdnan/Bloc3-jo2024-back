package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.filter.AdminSessionFilter;
import fr.studi.bloc3jo2024.filter.JwtAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AdminSessionFilter adminFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * Constructeur pour l'injection des dépendances.
     *
     * @param jwtFilter Filtre pour l'authentification JWT des utilisateurs.
     * @param adminFilter Filtre pour la gestion de session/authentification administrateur.
     * @param unauthorizedHandler Gestionnaire pour les échecs d'authentification (réponse 401).
     */
    public WebSecurityConfig(JwtAuthenticationFilter jwtFilter,
                             AdminSessionFilter adminFilter,
                             JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.jwtFilter = jwtFilter;
        this.adminFilter = adminFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    /**
     * Définit l'encodeur de mot de passe à utiliser pour l'application.
     * BCrypt est un choix robuste pour le hachage de mots de passe.
     *
     * @return une instance de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configure les règles CORS (Cross-Origin Resource Sharing) pour l'application.
     * Permet de contrôler quelles origines externes peuvent accéder aux ressources de l'API.
     * Essentiel pour les applications web où le frontend et le backend sont sur des domaines/ports différents.
     *
     * @return une source de configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(
                "https://bloc3-jo2024-front-eff05b08aaa7.herokuapp.com", // URL frontend Heroku
                "http://localhost:80",  // Développement local via Docker 
                "http://localhost:3000" // Développement local du frontend 
        ));
        // Méthodes HTTP autorisées (GET, POST, PUT, DELETE, OPTIONS, PATCH)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // En-têtes autorisés dans les requêtes
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-XSRF-TOKEN"
        ));
        // En-têtes que le navigateur peut lire dans la réponse
        config.setExposedHeaders(List.of(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"
        ));
        // Autorise l'envoi de cookies et d'en-têtes d'authentification (nécessaire pour JWT dans les en-têtes)
        config.setAllowCredentials(true);
        // Durée de mise en cache (en secondes) des résultats de la requête pre-flight OPTIONS
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Applique cette configuration CORS à toutes les routes de l'application
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Configure la chaîne de filtres de sécurité HTTP principale.
     * Définit la politique de session, la gestion des exceptions, les règles d'autorisation
     * pour les différents endpoints, et l'ordre des filtres personnalisés.
     *
     * @param http L'objet HttpSecurity à configurer.
     * @return La SecurityFilterChain construite.
     * @throws Exception Si une erreur de configuration survient.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Désactiver la protection CSRF (Cross-Site Request Forgery).
                .csrf(AbstractHttpConfigurer::disable)
                // Configurer le repository du contexte de sécurité.
                .securityContext(context -> context
                        .securityContextRepository(new RequestAttributeSecurityContextRepository())
                )
                // Gérer les exceptions d'authentification avec le point d'entrée personnalisé (JwtAuthenticationEntryPoint)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // Configurer la gestion de session pour qu'elle soit stateless.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Définir les règles d'autorisation pour les requêtes HTTP.
                .authorizeHttpRequests(auth -> auth
                        // Endpoints Actuator (santé, info) accessibles publiquement.
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        // Sécurisation de l'endpoint "env" de l'actuator, accessible uniquement aux administrateurs.
                        .requestMatchers(EndpointRequest.to("env")).hasRole("ADMIN")
                        // Endpoints d'authentification utilisateur (inscription, connexion, confirmation de compte) publics.
                        .requestMatchers("/auth/register", "/auth/login", "/auth/confirm").permitAll()
                        // Endpoints pour la réinitialisation de mot de passe (demande et exécution) publics.
                        .requestMatchers(HttpMethod.POST, "/auth/password-reset-request", "/auth/password-reset").permitAll()

                        // Endpoints d'authentification pour la partie administration publics.
                        .requestMatchers("/api/admin/auth/**").permitAll()

                        // Endpoint de test sécurisé nécessitant le rôle USER ou ADMIN.
                        .requestMatchers("/api/test/secured").hasAnyRole("USER", "ADMIN")

                        // Toutes les routes sous /api/admin/ (non couvertes par la règle permitAll précédente)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Autorise les requêtes vers le favicon sans authentification
                        .requestMatchers("/static/favicon.ico").permitAll()

                        // Toutes les autres requêtes (celles non matchées précédemment) nécessitent une authentification.
                        .anyRequest().authenticated()
                )
                // Ajoute le filtre JwtAuthenticationFilter avant le filtre standard UsernamePasswordAuthenticationFilter.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Ajoute le filtre AdminSessionFilter avant BasicAuthenticationFilter.
                .addFilterBefore(adminFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expose le bean AuthenticationManager, nécessaire pour le processus d'authentification manuel (ex: dans le contrôleur de login).
     * Récupéré depuis AuthenticationConfiguration.
     *
     * @param config La configuration d'authentification de Spring Security.
     * @return L'AuthenticationManager configuré.
     * @throws Exception Si une erreur survient lors de la récupération de l'AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}