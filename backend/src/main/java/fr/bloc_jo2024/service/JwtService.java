package fr.bloc_jo2024.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Clé secrète chargée depuis application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    // Durée d'expiration du token (en millisecondes)
    @Value("${jwt.expiration}")
    private long expirationTime;

    // Génère un token JWT à partir de l'email de l'utilisateur.
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrait l'email (subject) contenu dans un token JWT.
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Vérifie si un token JWT est valide.
    public boolean isTokenValid(String token, String email) {
        try {
            return email.equals(extractEmail(token)) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Génère une clé de signature sécurisée à partir de la clé secrète.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }
}