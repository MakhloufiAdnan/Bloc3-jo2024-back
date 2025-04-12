package fr.bloc_jo2024.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS pour tous les chemins et toutes les méthodes HTTP
        registry.addMapping("/**")  // Applique CORS à toutes les routes
                .allowedOrigins("http://localhost")  // Autorisation frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // Méthodes HTTP autorisées
                .allowedHeaders("*")  // Autorise tous les en-têtes
                .allowCredentials(true)  // Permet l'envoi de cookies
                .maxAge(3600);  // Caching de la configuration pendant 1 heure (3600 secondes)
    }
}