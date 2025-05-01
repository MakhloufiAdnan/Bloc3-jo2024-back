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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AdminSessionFilter adminFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * Constructeur qui injecte les filtres JWT et Admin.
     */
    public WebSecurityConfig(JwtAuthenticationFilter jwtFilter,
                             AdminSessionFilter adminFilter,
                             JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.jwtFilter = jwtFilter;
        this.adminFilter = adminFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    /**
     * Bean pour encoder les mots de passe avec BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force l'utilisation de BCrypt avec un coût de 12
    }

    /**
     * Configuration CORS pour autoriser les requêtes cross-origin.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("https://bloc3-jo2024-front-9f76b95d070a.herokuapp.com", "http://localhost:80"));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
        config.setAllowedHeaders(Arrays.asList("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Configure la chaîne de filtres de sécurité HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Désactivation de la protection CSRF pour les API REST
        http.csrf(AbstractHttpConfigurer::disable)

                // Gestion des exceptions d'authentification
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))

                // Gestion de session en mode sans état (stateless) avec JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Définition des autorisations
                .authorizeHttpRequests(auth -> auth

                        // Autoriser l'inscription, la connexion et la confirmation sans authentification
                        .requestMatchers("/auth/register", "/auth/login", "/auth/confirm").permitAll()

                        // Routes admin auth accessibles sans session
                        .requestMatchers("/api/admin/auth/**").permitAll()

                        // Toutes les autres routes admin nécessitent authentification
                        .requestMatchers("/api/admin/**").authenticated()

                        // Autoriser les requêtes PUT pour changer le mot de passe sans authentification
                        .requestMatchers(HttpMethod.PUT,"/auth/changePassword").permitAll()

                        // Toutes les autres requêtes nécessitent authentification
                        .anyRequest().authenticated()
                )

                // Ajout du filtre JWT avant l'authentification par formulaire
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Ajout du filtre Admin avant le filtre basique HTTP
                .addFilterBefore(adminFilter, BasicAuthenticationFilter.class);

        // Construction finale de la chaîne
        return http.build();
    }

    /**
     * Fournit l'AuthenticationManager nécessaire pour l'authentification.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}