package fr.studi.bloc3jo2024.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class AdminSessionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdminSessionFilter.class);

    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";
    private final ObjectMapper objectMapper;

    @Value("${admin.path.prefix:/api/admin}")
    private String adminPathPrefix;

    @Value("${admin.auth.path.prefix:/api/admin/auth}")
    private String adminAuthPathPrefix;

    /**
     * Constructeur pour l'injection de dépendances (si nécessaire).
     * @param objectMapper L'ObjectMapper pour la sérialisation JSON.
     */
    public AdminSessionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    /**
     * Logique principale du filtre.
     * Vérifie si la requête cible une route d'administration protégée (hors authentification)
     * et si une session d'administrateur valide existe.
     * Si non autorisé, une réponse HTTP 401 est envoyée.
     *
     * @param request     La requête HTTP entrante.
     * @param response    La réponse HTTP à construire.
     * @param filterChain La chaîne de filtres à exécuter.
     * @throws ServletException Si une erreur de servlet se produit.
     * @throws IOException      Si une erreur d'E/S se produit.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Le filtre s'applique aux routes sous adminPathPrefix qui ne sont PAS sous adminAuthPathPrefix.
        if (requestURI.startsWith(adminPathPrefix) && !requestURI.startsWith(adminAuthPathPrefix)) {
            log.debug("Requête pour une route admin protégée : {}", requestURI);
            HttpSession session = request.getSession(false); // Ne pas créer de session si elle n'existe pas.

            if (session == null || session.getAttribute(SESSION_ADMIN_LOGGED_IN) == null) {
                log.warn("Accès non autorisé à la route admin protégée {} : session admin manquante ou invalide.", requestURI);

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Utiliser la constante de MediaType
                response.setCharacterEncoding("UTF-8"); // Assurer l'encodage correct

                // Écrire une réponse JSON structurée
                Map<String, Object> errorDetails = Map.of(
                        "timestamp", System.currentTimeMillis(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "Unauthorized",
                        "message", "Accès refusé. Une session administrateur est requise.",
                        "path", requestURI
                );
                objectMapper.writeValue(response.getWriter(), errorDetails);
                return; // Arrêter le traitement de la chaîne de filtres.
            }
            log.debug("Session admin validée pour la route : {}", requestURI);
        }

        // Poursuivre avec les autres filtres de la chaîne.
        filterChain.doFilter(request, response);
    }
}