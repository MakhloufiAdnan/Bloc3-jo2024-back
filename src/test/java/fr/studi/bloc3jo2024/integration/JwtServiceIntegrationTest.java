package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=dGhpc2lzYXJlYWxseWxvbmdhbmRzZWN1cmVqd3RzZWNyZXRrZXlmb3JqbzIwMjRjZXR0ZUNsZWRvaXRldHJlU3VwZXJlU2VTdXBlcnRjbG9uZ3VlSmUgbmUgc2FpcyBwbHVzIHF1b2lkaXJl",
        "jwt.expiration=100000"
})
class JwtServiceIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldGenerateTokenContainingCorrectEmail() {
        // Arrange
        String email = "user@example.com";

        // Act
        String token = jwtService.generateToken(email);

        // Assert
        assertNotNull(token);
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void shouldReturnTrueForValidToken() {
        // Arrange
        String email = "valid@example.com";
        String token = jwtService.generateToken(email);

        // Act
        boolean result = jwtService.isTokenValid(token, email);

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForInvalidTokenSignature() {
        // Arrange
        String email = "hacker@example.com";
        String validToken = jwtService.generateToken(email);
        String tamperedToken = validToken + "tampered";

        // Act
        boolean result = jwtService.isTokenValid(tamperedToken, email);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        // Arrange
        String email = "expired@example.com";
        String validToken = jwtService.generateToken(email);

        // Récupérer la clé de signature depuis la configuration Spring
        String encodedSecret = applicationContext.getEnvironment().getProperty("jwt.secret");
        SecretKey signingKeyForTest = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        // Récupérer les claims du token valide
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKeyForTest)
                .build()
                .parseClaimsJws(validToken)
                .getBody();

        // Modifier la date d'expiration pour qu'elle soit dans le passé
        claims.setExpiration(new Date(System.currentTimeMillis() - 60_000));

        // Re-signer le token avec la date d'expiration modifiée
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .signWith(signingKeyForTest, SignatureAlgorithm.HS256)
                .compact();

        // Act
        boolean result = jwtService.isTokenValid(expiredToken, email);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldExtractClaimSuccessfully() {
        // Arrange
        String email = "claim@example.com";
        String token = jwtService.generateToken(email);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals(email, subject);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        // Arrange
        String malformedToken = "not.a.valid.token";

        // Act & Assert
        assertThrows(MalformedJwtException.class,
                () -> jwtService.extractClaim(malformedToken, Claims::getSubject));
    }
}