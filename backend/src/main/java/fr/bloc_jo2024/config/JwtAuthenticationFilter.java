package fr.bloc_jo2024.config;

import fr.bloc_jo2024.service.JwtService;
import fr.bloc_jo2024.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);

        // Si aucun token n'est présent, on laisse passer la demande
        // afin que la configuration de sécurité gère l'accès aux endpoints ouverts.
        if (token != null) {
            try {
                String email = jwtService.extractEmail(token);
                if (email != null
                        && SecurityContextHolder.getContext().getAuthentication() == null
                        && jwtService.isTokenValid(token, email)) {

                    // Charge l'utilisateur complet depuis la BDD
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalide ou expiré. Veuillez vous reconnecter.");
                    return;
                }
            } catch (JwtException e) {
                handleJwtException(response, e);
                return;
            } catch (Exception e) {
                respondWithError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.");
                return;
            }
        }

        // Continue le filtrage pour les endpoints publics ou si l'authentification a été établie
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        return (token != null && token.startsWith(BEARER_PREFIX)) ? token.substring(7) : null;
    }

    private void respondWithError(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }

    private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
        if (e instanceof ExpiredJwtException) {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expiré. Veuillez vous reconnecter.");
        } else if (e instanceof SignatureException) {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Signature du token invalide.");
        } else {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalide. Veuillez vérifier le token fourni.");
        }
    }
}
