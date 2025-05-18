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
import org.springframework.test.annotation.Rollback;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
class AuthTokenTemporaireServiceIntegrationTest {

    // L'annotation @Container avec un champ static assure que Testcontainers gère
    // le cycle de vie (start/stop) du conteneur pour toutes les méthodes de test de cette classe.
    @SuppressWarnings("resource") // L'avertissement IDE sur "try-with-resources" peut être ignoré ici.
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
            .withDatabaseName("test_service_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_service")
            .withPassword("test_pass_service");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always");
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
        Role userRole = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseThrow(() -> new IllegalStateException("Rôle USER non trouvé. Vérifiez data.sql."));

        Pays pays = paysRepository.findByNomPays("France Test Service").orElseGet(() -> {
            Pays newPays = new Pays();
            newPays.setNomPays("France Test Service"); // Nom unique pour ce contexte de test
            return paysRepository.saveAndFlush(newPays);
        });

        Adresse adresse = new Adresse();
        adresse.setNomRue("Rue de ServiceTest Setup");
        adresse.setNumeroRue(12);
        adresse.setCodePostal("75002");
        adresse.setVille("Paris");
        adresse.setPays(pays);
        adresseRepository.saveAndFlush(adresse);

        testUtilisateur = new Utilisateur();
        // Utilisation d'un email unique pour chaque exécution de setUp pour éviter les conflits potentiels
        // si le rollback n'était pas parfait ou pour des tests futurs non transactionnels.
        testUtilisateur.setEmail("servicetest_" + UUID.randomUUID().toString().substring(0,8) + "@example.com");
        testUtilisateur.setNom("ServiceNom");
        testUtilisateur.setPrenom("ServicePrenom");
        testUtilisateur.setDateNaissance(LocalDate.of(1995, 5, 15));
        testUtilisateur.setAdresse(adresse);
        testUtilisateur.setRole(userRole);
        testUtilisateur.setDateCreation(LocalDateTime.now());
        testUtilisateur.setVerified(true); // Ou false selon les besoins du test
        utilisateurRepository.saveAndFlush(testUtilisateur);
    }

    @Test
    void testCreateToken_persistsToken() {
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofMinutes(30);

        String rawToken = tokenService.createToken(testUtilisateur, type, validity);
        assertNotNull(rawToken, "Le token brut ne doit pas être null.");

        List<AuthTokenTemporaire> allTokens = tokenRepository.findAll();
        assertEquals(1, allTokens.size(), "Un seul token devrait exister en base après création.");
        AuthTokenTemporaire savedToken = allTokens.getFirst(); // Utilisation de get(0)

        assertTrue(passwordEncoder.matches(rawToken, savedToken.getTokenHache()), "Le token brut doit correspondre au haché en base.");
        assertEquals(testUtilisateur.getIdUtilisateur(), savedToken.getUtilisateur().getIdUtilisateur(), "Le token doit être lié au bon utilisateur.");
        assertEquals(type, savedToken.getTypeToken(), "Le type du token doit être correct.");
        assertFalse(savedToken.isUsed(), "Le token doit être marqué comme non utilisé initialement.");
        assertTrue(savedToken.getDateExpiration().isAfter(LocalDateTime.now().minusSeconds(1)), "Le token ne doit pas être expiré immédiatement.");
    }

    @Test
    void testValidateToken_validToken_returnsToken() {
        String rawToken = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(10));
        AuthTokenTemporaire result = tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD);

        assertNotNull(result, "La validation d'un token valide devrait retourner l'entité token.");
        assertEquals(TypeAuthTokenTemp.RESET_PASSWORD, result.getTypeToken());
        assertFalse(result.isUsed());

        AuthTokenTemporaire tokenInDb = tokenRepository.findById(result.getIdTokenTemp()).orElseThrow();
        assertFalse(tokenInDb.isUsed(), "La validation seule ne doit pas marquer le token comme utilisé.");
    }

    @Test
    void testValidateToken_expiredToken_throwsException() {
        String rawToken = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofSeconds(-1)); // Token déjà expiré

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        // CORRECTION: Vérifier l'égalité exacte du message attendu.
        assertEquals("Token expiré.", exception.getMessage(), "Message d'erreur pour token expiré incorrect.");

        tokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHache()))
                .findFirst()
                .ifPresent(tokenInDb -> assertFalse(tokenInDb.isUsed(), "Un token expiré ne doit pas être marqué comme utilisé."));
    }

    @Test
    void testValidateToken_usedToken_throwsException() {
        String rawToken = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHache()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le marquer comme utilisé."));
        tokenService.markAsUsed(tokenEntity);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        // CORRECTION: Vérifier l'égalité exacte du message attendu.
        assertEquals("Token déjà utilisé.", exception.getMessage(), "Message d'erreur pour token utilisé incorrect.");
    }

    @Test
    void testValidateToken_nonExistentToken_throwsException() {
        String nonExistentRawToken = "this-token-does-not-exist-" + UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(nonExistentRawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        // CORRECTION: Vérifier l'égalité exacte du message attendu.
        assertEquals("Token non trouvé.", exception.getMessage(), "Message d'erreur pour token inexistant incorrect.");
    }

    @Test
    void testValidateToken_wrongTypeToken_throwsException() {
        String rawToken = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.VALIDATION_EMAIL, Duration.ofMinutes(5));
        // Le service est censé lever IllegalStateException pour un type incorrect (après les autres vérifications)
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        // L'assertion originale avec contains devrait fonctionner si le message est bien celui-ci.
        assertTrue(exception.getMessage().startsWith("Type de token incorrect. Attendu: RESET_PASSWORD, Obtenu: VALIDATION_EMAIL"), "Message d'erreur pour type de token incorrect: " + exception.getMessage());
    }

    @Test
    void testMarkAsUsed_setsUsedFlag() {
        String rawToken = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHache()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le test markAsUsed."));
        tokenService.markAsUsed(tokenEntity);
        AuthTokenTemporaire updatedToken = tokenRepository.findById(tokenEntity.getIdTokenTemp())
                .orElseThrow(() -> new AssertionError("Token non trouvé après markAsUsed."));
        assertTrue(updatedToken.isUsed(), "Le flag 'isUsed' doit être vrai après markAsUsed.");
    }

    @Test
    void testPurgeExpiredTokens_removesExpiredOnly() {
        // Utilisation d'UUID pour garantir l'unicité des tokenHache pour ce test
        AuthTokenTemporaire expired1 = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("exp1"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.RESET_PASSWORD).dateExpiration(LocalDateTime.now().minusHours(1)).isUsed(false).build();
        AuthTokenTemporaire expiredAndUsed = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("expUsed"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL).dateExpiration(LocalDateTime.now().minusMinutes(30)).isUsed(true).build();
        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("valid"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.RESET_PASSWORD).dateExpiration(LocalDateTime.now().plusHours(1)).isUsed(false).build();
        AuthTokenTemporaire validUsedToken = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("validUsed"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.CONNEXION).dateExpiration(LocalDateTime.now().plusMinutes(30)).isUsed(true).build();

        tokenRepository.saveAll(List.of(expired1, expiredAndUsed, validToken, validUsedToken));
        assertEquals(4, tokenRepository.count(), "Il devrait y avoir 4 tokens avant la purge.");

        long purgedCount = tokenService.purgeExpiredTokens();
        assertEquals(2, purgedCount, "Seuls les 2 tokens expirés (expired1, expiredAndUsed) auraient dû être purgés.");

        List<AuthTokenTemporaire> remainingTokens = tokenRepository.findAll();
        assertEquals(2, remainingTokens.size(), "2 tokens non expirés devraient rester.");
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenHache().equals(validToken.getTokenHache())), "Le token valide et non utilisé doit rester.");
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenHache().equals(validUsedToken.getTokenHache())), "Le token valide et utilisé doit rester.");
    }

    @Test
    void testPurgeExpiredTokens_noExpiredTokens_purgesNone() {
        AuthTokenTemporaire validToken1 = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("valid1_purgeNone"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.RESET_PASSWORD).dateExpiration(LocalDateTime.now().plusHours(1)).isUsed(false).build();
        AuthTokenTemporaire validToken2Used = AuthTokenTemporaire.builder().utilisateur(testUtilisateur).tokenHache(passwordEncoder.encode("valid2Used_purgeNone"+UUID.randomUUID())).typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL).dateExpiration(LocalDateTime.now().plusMinutes(30)).isUsed(true).build();
        tokenRepository.saveAll(List.of(validToken1, validToken2Used));
        assertEquals(2, tokenRepository.count(), "Il devrait y avoir 2 tokens avant la purge.");

        long purgedCount = tokenService.purgeExpiredTokens();
        assertEquals(0, purgedCount, "Aucun token ne devrait être purgé s'ils ne sont pas expirés.");
        assertEquals(2, tokenRepository.count(), "Les 2 tokens non expirés devraient rester.");
    }
}
