package fr.bloc_jo2024.config;

import fr.bloc_jo2024.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extraire le token JWT depuis l'en-tête "Authorization"
        String token = extractToken(request);

        // Si le token est présent, l'authentification continue, sinon on répond avec une erreur
        if (token != null) {
            try {
                // Extraire l'email du token
                String email = jwtService.extractEmail(token);

                // Si un email est trouvé et que l'authentification n'est pas déjà établie
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Si le token est valide, on authentifie l'utilisateur
                    if (jwtService.isTokenValid(token, email)) {
                        authenticateUser(email);
                    } else {
                        // Si le token est invalide ou expiré, on envoie une erreur
                        respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalide ou expiré. Veuillez vous reconnecter.");
                        return;
                    }
                }
            } catch (JwtException e) {
                // Cas où le token JWT est invalide
                handleJwtException(response, e);
                return;
            } catch (Exception e) {
                // En cas d'autres erreurs générales, on renvoie une erreur générique
                respondWithError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.");
                return;
            }
        } else {
            // Si aucun token n'est trouvé, on renvoie une erreur
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT manquant ou mal formé.");
            return;
        }

        // Si tout est valide, on continue avec la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    // Méthode pour extraire le token JWT de l'en-tête "Authorization"
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return (token != null && token.startsWith("Bearer ")) ? token.substring(7) : null;
    }

    // Méthode pour authentifier l'utilisateur en utilisant son email
    private void authenticateUser(String email) {
        UserDetails userDetails = new User(email, "", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Méthode pour renvoyer une réponse d'erreur personnalisée
    private void respondWithError(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }

    // Méthode pour gérer les exceptions spécifiques au JWT (expiré ou signature invalide)
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
