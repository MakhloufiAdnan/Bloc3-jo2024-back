package fr.studi.bloc3jo2024.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminSessionFilter extends OncePerRequestFilter {

    // Indiquer qu'un administrateur est connecté.
    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";

    // Préfixe de l'URI pour les routes d'administration, configurable via la propriété 'admin.path.prefix'.
    // Valeur par défaut : '/api/admin'.
    @Value("${admin.path.prefix:/api/admin}")
    private String adminPathPrefix;

    // Préfixe de l'URI pour les routes d'authentification de l'administrateur, configurable via 'admin.auth.path.prefix'.
    // Valeur par défaut : '/api/admin/auth'.
    @Value("${admin.auth.path.prefix:/api/admin/auth}")
    private String adminAuthPathPrefix;

    /**
     * Méthode principale du filtre, exécutée pour chaque requête.
     * Vérifie si la requête cible une route d'administration (hors routes d'authentification)
     * et s'assure qu'une session d'administrateur valide existe.
     *
     * @param request     La requête HTTP.
     * @param response    La réponse HTTP.
     * @param filterChain La chaîne de filtres à poursuivre si l'administrateur est authentifié.
     * @throws ServletException Si une erreur de servlet survient.
     * @throws IOException      Si une erreur d'entrée/sortie survient.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Vérifie si l'URI de la requête commence par le préfixe des routes d'administration
        // et ne commence pas par le préfixe des routes d'authentification de l'administrateur.
        if (requestURI.startsWith(adminPathPrefix) && !requestURI.startsWith(adminAuthPathPrefix)) {
            HttpSession session = request.getSession(false);

            // Vérifie si la session existe et si l'attribut indiquant la connexion de l'administrateur est présent.
            if (session == null || session.getAttribute(SESSION_ADMIN_LOGGED_IN) == null) {

                // Si l'administrateur n'est pas connecté, renvoie une réponse 401 Unauthorized.
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Admin non connecté.\"}");
                return;// Arrête le traitement de la requête.
            }
        }

        // Si la requête ne concerne pas les routes d'administration protégées
        // ou si l'administrateur est connecté, poursuit le traitement de la requête par les filtres suivants.
        filterChain.doFilter(request, response);
    }
}