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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class AuthTokenTemporaireServiceIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenTemporaireServiceIntegrationTest.class);

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
    private ApplicationContext applicationContext;

    @Autowired
    private Environment env;

    private Utilisateur testUtilisateur;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAllInBatch();
        utilisateurRepository.deleteAllInBatch();
        adresseRepository.deleteAllInBatch();
        paysRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        log.info("Spring Environment - Datasource URL from @BeforeEach: {}", env.getProperty("spring.datasource.url"));
        log.info("Spring Environment - Datasource Username from @BeforeEach: {}", env.getProperty("spring.datasource.username"));
        assertTrue(AbstractPostgresIntegrationTest.postgresDBContainer.isRunning(), "PostgreSQL container should be running before setup.");
        assertEquals(AbstractPostgresIntegrationTest.postgresDBContainer.getJdbcUrl(), env.getProperty("spring.datasource.url"), "Spring datasource URL should match Testcontainer URL.");

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
        adresseRepository.saveAndFlush(adresse);

        testUtilisateur = new Utilisateur();
        testUtilisateur.setEmail("servicetest_integ_" + UUID.randomUUID().toString().substring(0,8) + "@example.com");
        testUtilisateur.setNom("ServiceNomInteg");
        testUtilisateur.setPrenom("ServicePrenomInteg");
        testUtilisateur.setDateNaissance(LocalDate.of(1990, 1, 1));
        testUtilisateur.setAdresse(adresse);
        testUtilisateur.setRole(userRole);
        testUtilisateur.setVerified(true);
        utilisateurRepository.saveAndFlush(testUtilisateur);
    }

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "ApplicationContext should not be null.");
        log.info("ApplicationContext loaded successfully!");
    }

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
        // Assuming Utilisateur entity has getIdUtilisateur() mapping to id_utilisateur_uuid
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

        tokenService.markAsUsed(tokenEntity);


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

        Adresse existingAdresse = testUtilisateur.getAdresse();
        Role existingRole = testUtilisateur.getRole();

        Utilisateur anotherUser = Utilisateur.builder()
                .email("anotheruser_" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("Another")
                .prenom("User")
                .dateNaissance(LocalDate.of(1995, 5, 5))
                .adresse(existingAdresse)
                .role(existingRole)
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
                .utilisateur(testUtilisateur)
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusMinutes(30))
                .isUsed(true)
                .build();
        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(idValid)
                .tokenHache(passwordEncoder.encode(idValid))
                .utilisateur(anotherUser)
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
        assertEquals(2, purgedCount);

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
}