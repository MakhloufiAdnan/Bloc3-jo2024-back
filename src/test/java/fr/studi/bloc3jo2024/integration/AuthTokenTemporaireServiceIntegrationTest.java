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
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@Rollback
class AuthTokenTemporaireServiceIntegrationTest {

    @Autowired
    private AuthTokenTemporaireService tokenService;

    @Autowired
    private AuthTokenTemporaireRepository tokenRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {

        // Creer un pays fictif
        Pays pays;
        pays = new Pays();
        pays.setNomPays("France");
        // Enregister le pays dans la base de données
        paysRepository.save(pays);

        // Creer une adresse fictive
        Adresse adresse;
        adresse = new Adresse();
        adresse.setNomRue("Rue de Test");
        adresse.setNumeroRue(10);
        adresse.setCodePostal("75001");
        adresse.setVille("Paris");
        adresse.setPays(pays);
        // Enregister l'adresse dans la base de données
        adresseRepository.save(adresse);

        // Initialiser le rôle USER s'il n'existe pas déjà
        Role userRole;
        userRole = roleRepository.findByTypeRole(TypeRole.USER).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setTypeRole(TypeRole.USER);
            return roleRepository.save(newRole);
        });

        // Créer un utilisateur fictif
        utilisateur = new Utilisateur();
        utilisateur.setEmail("test@example.com");
        utilisateur.setNom("Studi");
        utilisateur.setPrenom("Bob");
        utilisateur.setDateNaissance(LocalDate.of(2000, 1, 1));
        utilisateur.setAdresse(adresse);
        utilisateur.setRole(userRole);
        utilisateurRepository.save(utilisateur);
    }

    @Test
    void testCreateToken_persistsToken() {
        // Arrange
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofMinutes(30);

        // Enregister l'utilisateur avant de créer le jeton
        utilisateurRepository.save(utilisateur);

        // Act
        String rawToken = tokenService.createToken(utilisateur, type, validity);

        // Assert
        List<AuthTokenTemporaire> all = tokenRepository.findAll();
        assertEquals(1, all.size());
        AuthTokenTemporaire saved = all.getFirst();
        assertTrue(passwordEncoder.matches(rawToken, saved.getTokenHache()));
        assertEquals(utilisateur, saved.getUtilisateur());
        assertEquals(type, saved.getTypeToken());
    }

    @Test
    void testValidateToken_returnsValidToken() {
        // Arrange
        // Enregister utilisateur
        utilisateurRepository.save(utilisateur);
        String rawToken = tokenService.createToken(utilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(10));

        // Act
        AuthTokenTemporaire result = tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD);

        // Assert
        assertNotNull(result);
        assertEquals(TypeAuthTokenTemp.RESET_PASSWORD, result.getTypeToken());
        assertFalse(result.isUsed());
    }

    @Test
    void testMarkAsUsed_setsUsedFlag() {
        // Arrange
        // Enregister utilisateur
        utilisateurRepository.save(utilisateur);
        String rawToken = tokenService.createToken(utilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofMinutes(5));
        AuthTokenTemporaire token = tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD);

        // Act
        tokenService.markAsUsed(token);

        // Assert
        AuthTokenTemporaire updated = tokenRepository.findById(token.getIdTokenTemp()).orElseThrow();
        assertTrue(updated.isUsed());
    }

    @Test
    void testPurgeExpiredTokens_removesExpired() {
        // Arrange
        // Enregister utilisateur
        utilisateurRepository.save(utilisateur);
        tokenService.createToken(utilisateur, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofSeconds(-1)); // déjà expiré

        // Act
        long purged = tokenService.purgeExpiredTokens();

        // Assert
        assertEquals(1, purged);
        assertTrue(tokenRepository.findAll().isEmpty());
    }
}
