package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Nécessaire avec Testcontainers
@Transactional // Bonne pratique pour les tests d'intégration DB
class AuthTokenTemporaireServiceIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_auth_token_svc_db_" + UUID.randomUUID().toString().substring(0,8)) // Nom de DB unique
            .withUsername("test_user_auth_token")
            .withPassword("test_pass_auth_token");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Recommandé pour tests
    }

    @Autowired
    private AuthTokenTemporaireService tokenService;
    @Autowired
    private AuthTokenTemporaireRepository tokenRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AdresseRepository adresseRepository;
    @Autowired
    private PaysRepository paysRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur testUtilisateur;

    @BeforeEach
    void setUp() {
        // S'assurer que le rôle USER existe, sinon le créer
        Role userRole = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseGet(() -> {
                    Role newRole = Role.builder().typeRole(TypeRole.USER).build();
                    return roleRepository.saveAndFlush(newRole);
                });

        Pays pays = paysRepository.findByNomPays("France AuthTokenTest").orElseGet(() ->
                paysRepository.saveAndFlush(Pays.builder().nomPays("France AuthTokenTest").build())
        );

        Adresse adresse = adresseRepository.saveAndFlush(
                Adresse.builder()
                        .nomRue("Rue AuthToken Service")
                        .numeroRue(1)
                        .codePostal("75001")
                        .ville("Paris Auth")
                        .pays(pays)
                        .build()
        );

        testUtilisateur = utilisateurRepository.saveAndFlush(
                Utilisateur.builder()
                        .email("authtoken_integ_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                        .nom("AuthNomInteg")
                        .prenom("AuthPrenomInteg")
                        .dateNaissance(LocalDate.of(1990, 1, 1))
                        .adresse(adresse)
                        .role(userRole)
                        .isVerified(true)
                        .authentification(Authentification.builder().motPasseHache(passwordEncoder.encode("password123")).build()) // Ajout pour complétude
                        .build()
        );
        // Lier l'authentification à l'utilisateur si la relation est bidirectionnelle et gérée ainsi
        if (testUtilisateur.getAuthentification() != null) {
            testUtilisateur.getAuthentification().setUtilisateur(testUtilisateur);
            // Pas besoin de sauvegarder authentification explicitement si CascadeType.ALL est sur Utilisateur.authentification
        }
        utilisateurRepository.saveAndFlush(testUtilisateur);

    }

    @Test
    void testCreateToken_persistsToken() {
        // Arrange
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofMinutes(30);

        // Act
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, type, validity);

        // Assert
        assertNotNull(rawTokenIdentifier, "Le token brut (identifiant) ne doit pas être null.");

        // Le service stocke le rawTokenIdentifier dans tokenIdentifier et son hash dans tokenHache.
        AuthTokenTemporaire savedToken = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token créé non trouvé par son identifiant (raw token)."));

        assertEquals(rawTokenIdentifier, savedToken.getTokenIdentifier(), "L'identifiant du token stocké doit être le token brut.");
        assertTrue(passwordEncoder.matches(rawTokenIdentifier, savedToken.getTokenHache()), "Le token brut doit correspondre au haché en base.");
        assertEquals(testUtilisateur.getIdUtilisateur(), savedToken.getUtilisateur().getIdUtilisateur(), "Le token doit être lié au bon utilisateur.");
        assertEquals(type, savedToken.getTypeToken(), "Le type du token doit être correct.");
        assertFalse(savedToken.isUsed(), "Le token doit être marqué comme non utilisé initialement.");

        LocalDateTime now = LocalDateTime.now();
        assertTrue(savedToken.getDateExpiration().isAfter(now.minusSeconds(10)), "Le token ne doit pas être expiré (avec marge de 10s).");
        LocalDateTime expectedExpiration = now.plus(validity);
        // Comparaison avec une tolérance pour les petites différences d'exécution
        assertTrue(Math.abs(Duration.between(expectedExpiration, savedToken.getDateExpiration()).getSeconds()) < 10,
                "La date d'expiration n'est pas celle attendue (marge de 10s). Attendu: " + expectedExpiration + ", Obtenu: " + savedToken.getDateExpiration());
    }

    @Test
    void testValidateToken_validToken_returnsToken() {
        // Arrange
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(10));

        // Act
        AuthTokenTemporaire result = tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD);

        // Assert
        assertNotNull(result);
        assertEquals(rawTokenIdentifier, result.getTokenIdentifier());
        assertEquals(TypeAuthTokenTemp.RESET_PASSWORD, result.getTypeToken());
        assertFalse(result.isUsed());
    }

    @Test
    void testValidateToken_expiredToken_throwsIllegalArgumentException() {
        // Arrange
        String rawTokenForExpired = "expired-token-" + UUID.randomUUID().toString();
        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(rawTokenForExpired)
                .tokenHache(passwordEncoder.encode(rawTokenForExpired))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().minusMinutes(1)) // Expiré
                .isUsed(false)
                .build();
        tokenRepository.saveAndFlush(expiredToken);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawTokenForExpired, TypeAuthTokenTemp.RESET_PASSWORD));
        assertTrue(exception.getMessage().contains("Token expiré"), "Message d'erreur attendu: " + exception.getMessage());
    }

    @Test
    void testValidateToken_usedToken_throwsIllegalArgumentException() {
        // Arrange
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le marquer comme utilisé."));
        tokenService.markAsUsed(tokenEntity); // Marquer comme utilisé

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Token déjà utilisé.", exception.getMessage());
    }

    @Test
    void testValidateToken_nonExistentToken_throwsIllegalArgumentException() {
        // Arrange
        String nonExistentRawToken = "token-inexistant-" + UUID.randomUUID().toString();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(nonExistentRawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Token non trouvé.", exception.getMessage());
    }

    @Test
    void testValidateToken_wrongTypeToken_throwsIllegalStateException() {
        // Arrange
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.VALIDATION_EMAIL, Duration.ofMinutes(5));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Type de token incorrect. Attendu: RESET_PASSWORD, Obtenu: VALIDATION_EMAIL", exception.getMessage());
    }

    @Test
    void testMarkAsUsed_setsUsedFlag() {
        // Arrange
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le test markAsUsed."));
        assertFalse(tokenEntity.isUsed(), "Le token doit être initialement non utilisé.");

        // Act
        tokenService.markAsUsed(tokenEntity);

        // Assert
        // Recharger depuis la base pour vérifier la persistance du changement
        AuthTokenTemporaire updatedToken = tokenRepository.findById(tokenEntity.getIdTokenTemp())
                .orElseThrow(() -> new AssertionError("Token non trouvé après markAsUsed."));
        assertTrue(updatedToken.isUsed(), "Le flag 'isUsed' du token doit être à true après markAsUsed.");
    }

    @Test
    void testPurgeExpiredTokens_removesExpiredOnly() {
        // Arrange
        // CORRECTION: Utiliser des UUIDs valides pour tokenIdentifier
        String idExpired1 = UUID.randomUUID().toString();
        String idExpiredUsed = UUID.randomUUID().toString();
        String idValid = UUID.randomUUID().toString();
        String idValidUsed = UUID.randomUUID().toString();

        AuthTokenTemporaire expired1Entity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idExpired1)
                .tokenHache(passwordEncoder.encode(idExpired1)) // Hachage du token
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().minusHours(1)) // Expiré
                .isUsed(false)
                .build();
        AuthTokenTemporaire expiredAndUsedEntity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idExpiredUsed)
                .tokenHache(passwordEncoder.encode(idExpiredUsed))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusMinutes(30)) // Expiré
                .isUsed(true)
                .build();
        AuthTokenTemporaire validTokenEntity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid)
                .tokenHache(passwordEncoder.encode(idValid))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1)) // Valide
                .isUsed(false)
                .build();
        AuthTokenTemporaire validUsedTokenEntity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValidUsed)
                .tokenHache(passwordEncoder.encode(idValidUsed))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.CONNEXION)
                .dateExpiration(LocalDateTime.now().plusMinutes(30)) // Valide
                .isUsed(true)
                .build();

        tokenRepository.saveAllAndFlush(List.of(expired1Entity, expiredAndUsedEntity, validTokenEntity, validUsedTokenEntity));
        assertEquals(4, tokenRepository.count(), "Le nombre initial de tokens n'est pas correct.");

        // Act
        long purgedCount = tokenService.purgeExpiredTokens();

        // Assert
        assertEquals(2, purgedCount, "Le nombre de tokens purgés n'est pas correct (devrait être 2).");

        List<AuthTokenTemporaire> remainingTokens = tokenRepository.findAll();
        assertEquals(2, remainingTokens.size(), "Le nombre de tokens restants n'est pas correct.");
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idValid)), "Le token valide et non utilisé doit rester.");
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idValidUsed)), "Le token valide et utilisé doit rester.");

        assertFalse(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idExpired1)), "Le token expiré et non utilisé ne doit plus exister.");
        assertFalse(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idExpiredUsed)), "Le token expiré et utilisé ne doit plus exister.");
    }

    @Test
    void testPurgeExpiredTokens_noExpiredTokens_purgesNone() {
        // Arrange
        // CORRECTION: Utiliser des UUIDs valides pour tokenIdentifier
        String idValid1 = UUID.randomUUID().toString();
        String idValid2Used = UUID.randomUUID().toString();

        AuthTokenTemporaire validToken1Entity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid1)
                .tokenHache(passwordEncoder.encode(idValid1))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1)) // Valide
                .isUsed(false)
                .build();
        AuthTokenTemporaire validToken2UsedEntity = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid2Used)
                .tokenHache(passwordEncoder.encode(idValid2Used))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusMinutes(30)) // Valide
                .isUsed(true)
                .build();
        tokenRepository.saveAllAndFlush(List.of(validToken1Entity, validToken2UsedEntity));
        assertEquals(2, tokenRepository.count(), "Le nombre initial de tokens n'est pas correct.");

        // Act
        long purgedCount = tokenService.purgeExpiredTokens();

        // Assert
        assertEquals(0, purgedCount, "Aucun token ne devrait être purgé.");
        assertEquals(2, tokenRepository.count(), "Le nombre de tokens ne doit pas avoir changé.");
    }
}