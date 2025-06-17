package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.filter.AdminSessionFilter;
import fr.studi.bloc3jo2024.filter.JwtAuthenticationFilter;
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
                "https://bloc3-jo2024-front-eff05b08aaa7.herokuapp.com",
                "http://localhost:80",
                "http://localhost:3000",
                "http://127.0.0.1:5500", // Ajouté pour le développement local
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-XSRF-TOKEN"
        ));
        config.setExposedHeaders(List.of(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
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
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(context -> context
                        .securityContextRepository(new RequestAttributeSecurityContextRepository())
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Endpoints publics divers
                        .requestMatchers("/management/health", "/management/info", "/app-status").permitAll()

                        // Endpoints publics pour l'authentification des utilisateurs
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/confirm").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/password-reset-request", "/api/auth/password-reset").permitAll()

                        // Endpoint public pour le login du personnel de scan
                        .requestMatchers("/api/staff/login").permitAll()

                        // Endpoints sécurisés pour le rôle SCANNER
                        .requestMatchers("/api/billets/verifier/**").hasRole("SCANNER")
                        .requestMatchers("/api/billets/sync/**").hasRole("SCANNER")

                        // Endpoints sécurisés pour le rôle ADMIN
                        .requestMatchers("/management/env", "/api/admin/**", "/pages/home-admin.html").hasRole("ADMIN")

                        // Endpoints sécurisés pour les tests authentifiés
                        .requestMatchers("/api/test/secured").hasAnyRole("USER", "ADMIN")

                        // Toutes les autres requêtes doivent être authentifiées
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
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