package fr.studi.bloc3jo2024.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Spécifique pour les erreurs de signature
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTimeMillis; // Renommé pour clarifier l'unité

    // Durée de tolérance pour les décalages d'horloge (en secondes) lors de la validation des timestamps (exp, nbf)
    private static final long CLOCK_SKEW_SECONDS = 60L;

    /**
     * Génère une clé de signature HMAC-SHA à partir de la clé secrète configurée (encodée en Base64).
     *
     * @return La SecretKey pour signer et vérifier les JWTs.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token JWT pour un utilisateur donné (identifié par son email).
     * Le token inclut l'email comme sujet, la date d'émission et la date d'expiration.
     *
     * @param email L'email de l'utilisateur, qui servira de sujet ("subject") au JWT.
     * @return Le token JWT sous forme de chaîne de caractères.
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeMillis);

        // Log pour débogage des timestamps (surtout si des problèmes de fuseau horaire/iat persistent)
        log.debug("Génération du token pour l'email : {}. Date d'émission (iat): {}. Date d'expiration (exp): {}", email, now, expiryDate);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait toutes les revendications (claims) d'un token JWT.
     * Cette méthode gère la validation de la signature et des timestamps (exp, nbf)
     * en fonction de la configuration du parser (y compris `allowedClockSkewSeconds`).
     *
     * @param token Le token JWT à analyser.
     * @return Un objet Claims contenant toutes les informations du token.
     * @throws ExpiredJwtException Si le token est expiré.
     * @throws UnsupportedJwtException Si le token n'est pas dans un format attendu ou supporté.
     * @throws MalformedJwtException Si le token est malformé.
     * @throws SignatureException Si la signature du token est invalide.
     * @throws IllegalArgumentException Si l'argument token est null ou vide.
     * @throws JwtException Pour toute autre erreur liée au JWT lors de l'analyse.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS) // Tolérance pour les décalages d'horloge
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrait une revendication spécifique du token JWT en utilisant une fonction resolver.
     *
     * @param token Le token JWT.
     * @param claimsResolver Fonction pour extraire la revendication souhaitée.
     * @param <T> Le type de la revendication.
     * @return La valeur de la revendication.
     * @throws JwtException Si l'extraction échoue (par exemple, token invalide, claim manquant).
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Peut lever des exceptions JWT
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait l'email (sujet) d'un token JWT.
     *
     * @param token Le token JWT.
     * @return L'email de l'utilisateur.
     * @throws JwtException Si l'extraction échoue.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Vérifie si un token JWT est expiré.
     *
     * @param token Le token JWT.
     * @return true si le token est expiré, false sinon.
     * @throws JwtException Si la date d'expiration ne peut être extraite (token invalide).
     */
    private boolean isTokenExpired(String token) {
        // extractClaim gère déjà les exceptions si le token est invalide pour obtenir l'expiration
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Valide un token JWT : vérifie si l'email correspond au sujet du token et si le token n'est pas expiré.
     * Gère les exceptions JWT qui peuvent survenir lors de l'analyse du token.
     *
     * @param token Le token JWT à valider.
     * @param email L'email de l'utilisateur à vérifier par rapport au sujet du token.
     * @return true si le token est valide, false sinon.
     */
    public boolean isTokenValid(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token); // Peut lever JwtException
            // Vérifie la correspondance de l'email et que le token n'est pas expiré.
            // isTokenExpired peut aussi lever JwtException si le token est malformé au point où l'expiration ne peut être lue,
            // mais extractEmail l'aurait probablement déjà intercepté.
            return email.equals(extractedEmail) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Validation du JWT échouée pour l'email '{}' : Token expiré. Token : [{}...]", email, formatTokenForLog(token), e);
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Validation du JWT échouée pour l'email '{}' : Token non supporté. Token : [{}...]", email, formatTokenForLog(token), e);
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Validation du JWT échouée pour l'email '{}' : Token malformé. Token : [{}...]", email, formatTokenForLog(token), e);
            return false;
        } catch (SignatureException e) {
            log.warn("Validation du JWT échouée pour l'email '{}' : Signature invalide. Token : [{}...]", email, formatTokenForLog(token), e);
            return false;
        } catch (IllegalArgumentException e) {
            // Peut être levé par Jwts.parserBuilder() si token est null/vide
            log.warn("Validation du JWT échouée pour l'email '{}' : Argument invalide (token null/vide ?).", email, e);
            return false;
        } catch (JwtException e) { // Catch-all pour d'autres JwtException
            log.warn("Validation du JWT échouée pour l'email '{}' : Erreur JWT générique. Token : [{}...]", email, formatTokenForLog(token), e);
            return false;
        }
    }

    /**
     * Formate le token pour l'affichage dans les logs (tronqué pour la sécurité/lisibilité).
     */
    private String formatTokenForLog(String token) {
        if (token == null || token.isEmpty()) {
            return "null or empty";
        }
        return token.length() > 20 ? token.substring(0, 20) + "..." : token;
    }
}