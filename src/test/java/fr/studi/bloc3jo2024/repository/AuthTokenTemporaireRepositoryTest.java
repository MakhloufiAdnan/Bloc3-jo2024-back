package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthTokenTemporaireRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AuthTokenTemporaireRepository tokenRepository;

    @Autowired
    private TestEntityManager entityManager; // Pour préparer les données de test

    private Utilisateur testUser;

    /**
     * Nettoie les données et prépare l'utilisateur de test avant chaque méthode de test.
     * Assure l'isolation des tests avec le conteneur partagé.
     */
    @BeforeEach
    void setUpTestData() {
        // --- Nettoyage des données ---
        // L'ordre est important en raison des clés étrangères.
        entityManager.getEntityManager().createQuery("DELETE FROM AuthTokenTemporaire").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Utilisateur").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Role").executeUpdate();
        entityManager.flush(); // S'assurer que les suppressions sont commitées

        // --- Préparation des données de test de base ---
        Role userRoleEntity = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst()
                .orElseGet(() -> entityManager.persistFlushFind(Role.builder().typeRole(TypeRole.USER).build()));

        Pays francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "FranceRepoTest")
                .getResultStream().findFirst()
                .orElseGet(() -> entityManager.persistFlushFind(Pays.builder().nomPays("FranceRepoTest").build()));

        Adresse userAdresse = entityManager.persistFlushFind(Adresse.builder()
                .nomRue("1 rue de la Paix TestRepo")
                .numeroRue(1)
                .ville("ParisTestRepo")
                .codePostal("75001")
                .pays(francePays)
                .build());

        testUser = Utilisateur.builder()
                .email("testuser.repo." + UUID.randomUUID().toString().substring(0,8) + "@example.com") // Email unique
                .nom("RepoUserNom")
                .prenom("RepoUserPrenom")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(userRoleEntity)
                .adresse(userAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(true)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }


    @Test
    void testCreationTokenTemporaire() {
        String tokenIdentifier = UUID.randomUUID().toString();
        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
                .tokenIdentifier(tokenIdentifier)
                .tokenHache("hashed123ForCreationTest")
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusDays(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();

        AuthTokenTemporaire savedToken = tokenRepository.save(token);
        entityManager.flush();

        Optional<AuthTokenTemporaire> retrievedTokenOpt = tokenRepository.findById(savedToken.getIdTokenTemp());
        assertThat(retrievedTokenOpt).isPresent();
        AuthTokenTemporaire retrievedToken = retrievedTokenOpt.get();

        assertThat(retrievedToken.getIdTokenTemp()).isNotNull();
        assertThat(retrievedToken.getTokenIdentifier()).isEqualTo(tokenIdentifier);
        assertThat(retrievedToken.getTypeToken()).isEqualTo(TypeAuthTokenTemp.VALIDATION_EMAIL);
        assertThat(retrievedToken.getTokenHache()).isEqualTo("hashed123ForCreationTest");
        assertThat(retrievedToken.getDateExpiration()).isAfter(LocalDateTime.now());
        assertThat(retrievedToken.getUtilisateur().getIdUtilisateur()).isEqualTo(testUser.getIdUtilisateur());
    }

    @Test
    void testFindByTokenHache() {
        String uniqueHash = "uniqueHashedForFindBy_" + UUID.randomUUID(); // Assurer l'unicité
        String tokenIdentifier = UUID.randomUUID().toString();

        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
                .tokenIdentifier(tokenIdentifier)
                .tokenHache(uniqueHash)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(2))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(token);
        entityManager.flush();

        Optional<AuthTokenTemporaire> foundTokenOptional = tokenRepository.findByTokenHache(uniqueHash);
        assertThat(foundTokenOptional)
                .isPresent()
                .hasValueSatisfying(foundToken -> {
                    assertThat(foundToken.getTokenIdentifier()).isEqualTo(tokenIdentifier);
                    assertThat(foundToken.getTokenHache()).isEqualTo(uniqueHash);
                    assertThat(foundToken.getTypeToken()).isEqualTo(TypeAuthTokenTemp.RESET_PASSWORD);
                    assertThat(foundToken.getUtilisateur().getIdUtilisateur()).isEqualTo(testUser.getIdUtilisateur());
                });

        assertThat(tokenRepository.findByTokenHache("nonExistentHash")).isNotPresent();
    }

    @Test
    void testFindByUtilisateurAndTypeToken() {
        String tokenIdentifier = UUID.randomUUID().toString();
        AuthTokenTemporaire token1 = AuthTokenTemporaire.builder()
                .tokenIdentifier(tokenIdentifier)
                .tokenHache("hashForUserAndTypeTest_" + UUID.randomUUID()) // Assurer l'unicité
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusDays(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(token1);
        entityManager.flush();

        Optional<AuthTokenTemporaire> foundTokenOptional = tokenRepository.findByUtilisateurAndTypeToken(testUser, TypeAuthTokenTemp.VALIDATION_EMAIL);
        assertThat(foundTokenOptional)
                .isPresent()
                .hasValueSatisfying(foundToken -> {
                    assertThat(foundToken.getTokenIdentifier()).isEqualTo(tokenIdentifier);
                    assertThat(foundToken.getTokenHache()).isEqualTo(token1.getTokenHache());
                });


        assertThat(tokenRepository.findByUtilisateurAndTypeToken(testUser, TypeAuthTokenTemp.RESET_PASSWORD))
                .isNotPresent();
    }

    @Test
    void testDeleteByDateExpirationBefore() {
        String expiredTokenIdentifier = UUID.randomUUID().toString();
        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(expiredTokenIdentifier)
                .tokenHache("expiredTokenForDeleteTest_" + UUID.randomUUID()) // Assurer l'unicité
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusDays(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(expiredToken);

        String validTokenIdentifier = UUID.randomUUID().toString();
        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenIdentifier(validTokenIdentifier)
                .tokenHache("validTokenForDeleteTest_" + UUID.randomUUID()) // Assurer l'unicité
                .typeToken(TypeAuthTokenTemp.CONNEXION)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(validToken);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        long deletedCount = tokenRepository.deleteByDateExpirationBefore(now);
        entityManager.flush();
        entityManager.clear();

        assertThat(deletedCount).isEqualTo(1);
        // Pour vérifier la suppression
        assertThat(tokenRepository.findByTokenIdentifier(expiredTokenIdentifier)).isNotPresent();
        assertThat(tokenRepository.findByTokenIdentifier(validTokenIdentifier)).isPresent();
    }
}