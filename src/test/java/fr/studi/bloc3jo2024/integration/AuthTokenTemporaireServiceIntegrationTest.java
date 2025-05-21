/*
package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional; // Recommended for test method rollback
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers // Annotation to enable Testcontainers support
@SpringBootTest
@ActiveProfiles("test") // Keep this to load any other test-specific beans if needed
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Crucial: tells Spring Boot not to replace the Testcontainer datasource
@Transactional // Optional: Rolls back transactions after each test method by default
class AuthTokenTemporaireServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenTemporaireServiceIntegrationTest.class);

    // Define the PostgreSQL Testcontainer
    @SuppressWarnings("resource")
    @Container // Marks this as a Testcontainer-managed container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_service_db_" + UUID.randomUUID().toString().substring(0, 8))
            .withUsername("test_user_service")
            .withPassword("test_pass_service")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("postgres-service-test")).withPrefix("DB-SERVICE-TEST"));

    // Dynamically provide the datasource properties to Spring
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Use create-drop for tests
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always"); // If you have schema.sql/data.sql in test/resources for base data

        log.info("PostgreSQL Testcontainer is running: {}", postgresDBContainer.isRunning());
        log.info("Dynamic properties registered for Testcontainer: URL={}, User={}",
                postgresDBContainer.getJdbcUrl(), postgresDBContainer.getUsername());
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

    @Autowired
    private ApplicationContext applicationContext; // To verify context loads

    @Autowired
    private Environment env; // To verify properties

    private Utilisateur testUtilisateur;

    @BeforeEach
    void setUp() {
        // Verify that Spring is using the Testcontainer properties
        log.info("Spring Environment - Datasource URL from @BeforeEach: {}", env.getProperty("spring.datasource.url"));
        log.info("Spring Environment - Datasource Username from @BeforeEach: {}", env.getProperty("spring.datasource.username"));
        assertTrue(postgresDBContainer.isRunning(), "PostgreSQL container should be running before setup.");
        assertEquals(postgresDBContainer.getJdbcUrl(), env.getProperty("spring.datasource.url"), "Spring datasource URL should match Testcontainer URL.");

        Role userRole = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseGet(() -> roleRepository.saveAndFlush(Role.builder().typeRole(TypeRole.USER).build()));

        Pays pays = paysRepository.findByNomPays("France Test Service Integration").orElseGet(() -> {
            Pays newPays = new Pays();
            newPays.setNomPays("France Test Service Integration");
            return paysRepository.saveAndFlush(newPays);
        });

        Adresse adresse = new Adresse();
        adresse.setNomRue("Rue de ServiceTest Setup Integration");
        adresse.setNumeroRue(123);
        adresse.setCodePostal("75003");
        adresse.setVille("Paris Test");
        adresse.setPays(pays);
        // It's better to save entities that don't have cascades from the owning side first, or ensure proper cascade settings.
        // Here, Pays should be saved before Adresse if Adresse has a foreign key to Pays and cascade is not from Adresse to Pays.
        // And Adresse should be saved before Utilisateur.
        adresseRepository.saveAndFlush(adresse);

        testUtilisateur = new Utilisateur();
        testUtilisateur.setEmail("servicetest_integ_" + UUID.randomUUID().toString().substring(0,8) + "@example.com");
        testUtilisateur.setNom("ServiceNomInteg");
        testUtilisateur.setPrenom("ServicePrenomInteg");
        testUtilisateur.setDateNaissance(LocalDate.of(1990, 1, 1));
        testUtilisateur.setAdresse(adresse); // Set the managed Adresse entity
        testUtilisateur.setRole(userRole);   // Set the managed Role entity
        testUtilisateur.setVerified(true);
        utilisateurRepository.saveAndFlush(testUtilisateur);
    }

    @Test
    void contextLoads() {
        // Simple test to ensure the application context loads successfully with all configurations
        assertNotNull(applicationContext, "ApplicationContext should not be null.");
        log.info("ApplicationContext loaded successfully!");
    }

    // ... (Your existing test methods: testCreateToken_persistsToken, etc.)
    // Ensure these tests are compatible with the ddl-auto=create-drop strategy
    // (i.e., data created in @BeforeEach will be available for each test, and schema is fresh)

    @Test
    void testCreateToken_persistsToken() {
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofMinutes(30);

        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, type, validity);
        assertNotNull(rawTokenIdentifier, "Le token brut (identifiant) ne doit pas être null.");

        AuthTokenTemporaire savedToken = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token créé non trouvé par son identifiant."));

        assertEquals(rawTokenIdentifier, savedToken.getTokenIdentifier(), "L'identifiant du token ne correspond pas.");
        assertTrue(passwordEncoder.matches(rawTokenIdentifier, savedToken.getTokenHache()), "Le token brut doit correspondre au haché en base.");
        assertEquals(testUtilisateur.getIdUtilisateur(), savedToken.getUtilisateur().getIdUtilisateur(), "Le token doit être lié au bon utilisateur.");
        assertEquals(type, savedToken.getTypeToken(), "Le type du token doit être correct.");
        assertFalse(savedToken.isUsed(), "Le token doit être marqué comme non utilisé initialement.");
        assertTrue(savedToken.getDateExpiration().isAfter(LocalDateTime.now().minusSeconds(5)), "Le token ne doit pas être expiré (avec marge).");
    }

    @Test
    void testValidateToken_validToken_returnsToken() {
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(10));
        AuthTokenTemporaire result = tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD);

        assertNotNull(result);
        assertEquals(rawTokenIdentifier, result.getTokenIdentifier());
        assertEquals(TypeAuthTokenTemp.RESET_PASSWORD, result.getTypeToken());
        assertFalse(result.isUsed());
    }

    @Test
    void testValidateToken_expiredToken_throwsException() {
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofSeconds(-1)); // Token déjà expiré

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Token expiré.", exception.getMessage());
    }

    @Test
    void testValidateToken_usedToken_throwsException() {
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le marquer comme utilisé."));
        tokenService.markAsUsed(tokenEntity);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Token déjà utilisé.", exception.getMessage());
    }

    @Test
    void testValidateToken_nonExistentToken_throwsException() {
        String nonExistentRawToken = "token-inexistant-" + UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tokenService.validateToken(nonExistentRawToken, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Token non trouvé.", exception.getMessage());
    }

    @Test
    void testValidateToken_wrongTypeToken_throwsException() {
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.VALIDATION_EMAIL, Duration.ofMinutes(5));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> tokenService.validateToken(rawTokenIdentifier, TypeAuthTokenTemp.RESET_PASSWORD));
        assertEquals("Type de token incorrect. Attendu: RESET_PASSWORD, Obtenu: VALIDATION_EMAIL", exception.getMessage());
    }

    @Test
    void testMarkAsUsed_setsUsedFlag() {
        String rawTokenIdentifier = tokenService.createToken(testUtilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire tokenEntity = tokenRepository.findByTokenIdentifier(rawTokenIdentifier)
                .orElseThrow(() -> new AssertionError("Token non trouvé pour le test markAsUsed."));

        AuthTokenTemporaire tokenParam = new AuthTokenTemporaire();
        tokenParam.setIdTokenTemp(tokenEntity.getIdTokenTemp());

        tokenService.markAsUsed(tokenParam);

        AuthTokenTemporaire updatedToken = tokenRepository.findById(tokenEntity.getIdTokenTemp())
                .orElseThrow(() -> new AssertionError("Token non trouvé après markAsUsed."));
        assertTrue(updatedToken.isUsed());
    }

    @Test
    void testPurgeExpiredTokens_removesExpiredOnly() {
        String idExpired1 = UUID.randomUUID().toString();
        String idExpiredUsed = UUID.randomUUID().toString();
        String idValid = UUID.randomUUID().toString();
        String idValidUsed = UUID.randomUUID().toString();

        // Create a secondary user for some tokens to avoid unique constraint issues if tokenIdentifier is unique globally
        // Or ensure tokenIdentifier is unique per user if that's the logic
        Utilisateur anotherUser = Utilisateur.builder()
                .email("anotheruser_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("Another")
                .prenom("User")
                .dateNaissance(LocalDate.of(1995, 5, 5))
                .adresse(testUtilisateur.getAdresse()) // re-use address or create new
                .role(testUtilisateur.getRole())       // re-use role
                .isVerified(true)
                .build();
        utilisateurRepository.saveAndFlush(anotherUser);


        AuthTokenTemporaire expired1 = AuthTokenTemporaire.builder()
                .tokenIdentifier(idExpired1)
                .tokenHache(passwordEncoder.encode(idExpired1))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().minusHours(1))
                .isUsed(false)
                .build();
        AuthTokenTemporaire expiredAndUsed = AuthTokenTemporaire.builder()
                .tokenIdentifier(idExpiredUsed)
                .tokenHache(passwordEncoder.encode(idExpiredUsed))
                .utilisateur(testUtilisateur) // Can use testUtilisateur or anotherUser
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusMinutes(30))
                .isUsed(true)
                .build();
        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid)
                .tokenHache(passwordEncoder.encode(idValid))
                .utilisateur(anotherUser) // Using another user to ensure variety
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();
        AuthTokenTemporaire validUsedToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValidUsed)
                .tokenHache(passwordEncoder.encode(idValidUsed))
                .utilisateur(anotherUser)
                .typeToken(TypeAuthTokenTemp.CONNEXION)
                .dateExpiration(LocalDateTime.now().plusMinutes(30))
                .isUsed(true)
                .build();

        tokenRepository.saveAllAndFlush(List.of(expired1, expiredAndUsed, validToken, validUsedToken));
        assertEquals(4, tokenRepository.count());

        long purgedCount = tokenService.purgeExpiredTokens();
        assertEquals(2, purgedCount); // expired1 and expiredAndUsed should be purged

        List<AuthTokenTemporaire> remainingTokens = tokenRepository.findAll();
        assertEquals(2, remainingTokens.size());
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idValid)), "Le token valide et non utilisé doit rester.");
        assertTrue(remainingTokens.stream().anyMatch(t -> t.getTokenIdentifier().equals(idValidUsed)), "Le token valide et utilisé doit rester.");
    }

    @Test
    void testPurgeExpiredTokens_noExpiredTokens_purgesNone() {
        String idValid1 = UUID.randomUUID().toString();
        String idValid2Used = UUID.randomUUID().toString();

        AuthTokenTemporaire validToken1 = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid1)
                .tokenHache(passwordEncoder.encode(idValid1))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();
        AuthTokenTemporaire validToken2Used = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid2Used)
                .tokenHache(passwordEncoder.encode(idValid2Used))
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusMinutes(30))
                .isUsed(true)
                .build();
        tokenRepository.saveAllAndFlush(List.of(validToken1, validToken2Used));
        assertEquals(2, tokenRepository.count());

        long purgedCount = tokenService.purgeExpiredTokens();
        assertEquals(0, purgedCount);
        assertEquals(2, tokenRepository.count());
    }
}*/