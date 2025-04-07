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

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT manquant ou mal formé.");
            return;
        }

        try {
            String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.isTokenValid(token, email)) {
                authenticateUser(email);  // Removed the token parameter here
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

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        return (token != null && token.startsWith(BEARER_PREFIX)) ? token.substring(7) : null;
    }

    private void authenticateUser(String email) {  // Removed the token parameter here
        UserDetails userDetails = new User(email, "", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
tpServletResponse.SC_UNAUTHORIZED, "Token expiré. Veuillez vous reconnecter.");
        } else if (e instanceof SignatureException) {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Signature du token invalide.");
        } else {
            respondWithError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalide. Veuillez vérifier le token fourni.");
        }
    }
}