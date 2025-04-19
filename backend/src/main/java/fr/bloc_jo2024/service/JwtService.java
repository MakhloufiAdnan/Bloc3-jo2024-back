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

    /**
     * Génère un token JWT à partir de l'email de l'utilisateur.
     * @param email L'email de l'utilisateur à inclure dans le token.
     * @return Le token JWT généré.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait l'email contenu dans un token JWT.
     * @param token Le token JWT à analyser.
     * @return L'email de l'utilisateur (le sujet du token).
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait un claim spécifique du token JWT en utilisant un resolver de claims.
     * @param token Le token JWT à analyser.
     * @param claimsResolver La fonction pour extraire le claim souhaité des claims.
     * @param <T> Le type du claim à extraire.
     * @return La valeur du claim extrait.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Vérifie si le token JWT est valide pour l'email fourni.
     * @param token Le token JWT à vérifier.
     * @param email L'email de l'utilisateur à comparer avec le sujet du token.
     * @return true si le token est valide pour cet email et n'est pas expiré, false sinon.
     */
    public boolean isTokenValid(String token, String email) {
        try {
            return email.equals(extractEmail(token)) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Vérifie si le token JWT est expiré.
     * @param token Le token JWT à vérifier.
     * @return true si le token est expiré, false sinon.
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Extrait toutes les claims du token JWT.
     * @param token Le token JWT à analyser.
     * @return L'objet Claims contenant toutes les informations du token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Génère une clé de signature sécurisée à partir de la clé secrète configurée.
     * @return La clé secrète pour la signature JWT.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }
}