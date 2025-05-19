package fr.studi.bloc3jo2024.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component("unauthorizedHandler")
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Cette méthode est appelée par Spring Security lorsqu'un utilisateur non authentifié
     * tente d'accéder à une ressource sécurisée et qu'une AuthenticationException est levée.
     *
     * @param request       La requête HTTP qui a déclenché l'AuthenticationException.
     * @param response      La réponse HTTP à envoyer au client.
     * @param authException L'exception qui a provoqué l'appel de ce point d'entrée.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de l'écriture de la réponse.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Accès non autorisé pour la requête : {} {} - Raison : {}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Utilise la constante de MediaType pour le type de contenu
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // Définit le statut HTTP à 401

        // Construction du corps de la réponse JSON
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis()); // Horodatage de l'erreur
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED); // Code de statut HTTP
        body.put("error", "Unauthorized"); // Terme standard pour l'erreur HTTP 401
        body.put("message", "Accès refusé. Une authentification est requise pour accéder à cette ressource.");
        body.put("path", request.getServletPath()); // Chemin de la requête ayant échoué

        // Écrit la map JSON dans le flux de sortie de la réponse
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}