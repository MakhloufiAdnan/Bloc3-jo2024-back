package fr.studi.bloc3jo2024.filter;

import fr.studi.bloc3jo2024.service.DetailUtilisateurService;
import fr.studi.bloc3jo2024.service.JwtService;
import fr.studi.bloc3jo2024.service.impl.DetailUtilisateurServiceImpl; // Assurez-vous que cet import est correct
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final DetailUtilisateurService detailUtilisateurService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(JwtService jwtService, DetailUtilisateurService detailUtilisateurService) {
        this.jwtService = jwtService;
        this.detailUtilisateurService = detailUtilisateurService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String token = extractTokenFromRequest(request);

        if (token == null) {
            log.trace("Aucun token JWT trouvé dans l'en-tête Authorization pour la requête : {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Token JWT trouvé, tentative de validation pour la requête : {}", request.getRequestURI());

        try {
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(token, email)) {
                    UserDetails userDetails = detailUtilisateurService.loadUserByUsername(email);

                    if (userDetails instanceof DetailUtilisateurServiceImpl) {
                        // Cast pour accéder à l'entité Utilisateur sous-jacente si votre UserDetails l'encapsule
                        fr.studi.bloc3jo2024.entity.Utilisateur underlyingUser = ((DetailUtilisateurServiceImpl) userDetails).utilisateur();
                        log.info("[FILTRE DEBUG] Pour la requête {}: Utilisateur: {}, isVerified de l'entité: {}, userDetails.isEnabled(): {}",
                                request.getRequestURI(), email, underlyingUser.isVerified(), userDetails.isEnabled());
                    } else {
                        // Log générique si le cast n'est pas possible ou si vous utilisez une autre implémentation de UserDetails
                        log.info("[FILTRE DEBUG] Pour la requête {}: Utilisateur: {}, userDetails.isEnabled(): {}",
                                request.getRequestURI(), email, userDetails.isEnabled());
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Utilisateur '{}' authentifié avec succès via JWT par le filtre pour la requête : {}", email, request.getRequestURI());
                } else {
                    log.warn("Token JWT marqué comme invalide par jwtService.isTokenValid() pour l'email (potentiel) '{}' et la requête : {}. La chaîne de filtres continue.", email, request.getRequestURI());
                }
            } else if (email == null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.warn("L'email extrait du token est null pour la requête : {}. Le token pourrait être problématique ou le contexte d'authentification déjà défini.", request.getRequestURI());
            }


        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expiré pour la requête {}: {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, e);
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT non supporté pour la requête {}: {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, e);
            return;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformé pour la requête {}: {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, e);
            return;
        } catch (SignatureException e) {
            log.warn("Signature du token JWT invalide pour la requête {}: {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, e);
            return;
        } catch (IllegalArgumentException e) {
            log.warn("Argument invalide lors du traitement JWT pour la requête {}: {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, e);
            return;
        } catch (UsernameNotFoundException e) {
            log.warn("Utilisateur non trouvé pour l'email extrait du JWT lors de la requête {} : {}", request.getRequestURI(), e.getMessage());
            handleJwtRelatedException(response, new JwtException("Utilisateur du token non trouvé.", e));
            return;
        } catch (Exception e) {
            log.error("Erreur interne du serveur lors du traitement du token JWT pour la requête {}: {}", request.getRequestURI(), e.getMessage(), e);
            respondWithError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur interne lors de la validation de l'authentification.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l'en-tête "Authorization" de la requête.
     * Package-private pour la testabilité.
     */
    String extractTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            if (token.isBlank()) {
                log.warn("Token JWT vide fourni après le préfixe Bearer.");
                return null;
            }
            return token;
        }
        return null;
    }

    private void respondWithError(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }

    private void handleJwtRelatedException(HttpServletResponse response, Exception e) throws IOException {
        String errorMessage;
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;

        switch (e) {
            case ExpiredJwtException ex -> errorMessage = "Token JWT expiré. Veuillez vous reconnecter.";
            case SignatureException ex -> errorMessage = "Signature du token JWT invalide.";
            case MalformedJwtException ex -> errorMessage = "Token JWT malformé.";
            case UnsupportedJwtException ex -> errorMessage = "Token JWT non supporté.";
            case IllegalArgumentException ex -> errorMessage = "Argument de token JWT invalide ou format incorrect.";
            case JwtException jwtEx -> errorMessage = jwtEx.getMessage() != null && !jwtEx.getMessage().isBlank() ? jwtEx.getMessage() : "Token JWT invalide.";
            default -> {
                log.error("Exception inattendue passée à handleJwtRelatedException: {}", e.getMessage(), e);
                errorMessage = "Erreur de validation du token inattendue.";
            }
        }
        respondWithError(response, statusCode, errorMessage);
    }
}
