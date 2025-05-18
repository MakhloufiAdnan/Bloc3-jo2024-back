package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthTokenTemporaireRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
            .withDatabaseName("test_repo_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user")
            .withPassword("test_pass");

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
    private AuthTokenTemporaireRepository tokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur testUser;

    @BeforeEach
    void setUpTestData() {
        Role userRoleEntity;
        userRoleEntity = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :type", Role.class)
                .setParameter("type", TypeRole.USER)
                .getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Le rôle USER n'a pas été trouvé. Vérifiez data.sql."));

        // On crée toujours le pays pour ce test.
        Pays francePays;
        francePays = new Pays();
        francePays.setNomPays("France");
        entityManager.persist(francePays);

        // On crée toujours l'adresse pour ce test.
        Adresse userAdresse;
        userAdresse = Adresse.builder()
                .nomRue("1 rue de la Paix")
                .numeroRue(1)
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .build();
        entityManager.persist(userAdresse);

        testUser = Utilisateur.builder()
                .email("testuser.repo@example.com")
                .nom("RepoUserNom")
                .prenom("RepoUserPrenom")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(userRoleEntity)
                .adresse(userAdresse)
                .dateCreation(LocalDateTime.now())
                .isVerified(false)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }


    @Test
    void testCreationTokenTemporaire() {
        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
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
        AuthTokenTemporaire retrievedToken = retrievedTokenOpt.get(); // Correction ici si c'était sur un Optional et non une List

        assertThat(retrievedToken.getIdTokenTemp()).isNotNull();
        assertThat(retrievedToken.getTypeToken()).isEqualTo(TypeAuthTokenTemp.VALIDATION_EMAIL);
        assertThat(retrievedToken.getTokenHache()).isEqualTo("hashed123ForCreationTest");
        assertThat(retrievedToken.getDateExpiration()).isAfter(LocalDateTime.now());
        assertThat(retrievedToken.getUtilisateur().getIdUtilisateur()).isEqualTo(testUser.getIdUtilisateur());
    }
    // ... (autres tests de AuthTokenTemporaireRepositoryTest restent similaires) ...
    // Assurez-vous que les autres tests utilisent entityManager.persist et entityManager.flush si besoin.

    @Test
    void testFindByTokenHache() {
        String uniqueHash = "uniqueHashedForFindBy";
        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
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
                    assertThat(foundToken.getTokenHache()).isEqualTo(uniqueHash);
                    assertThat(foundToken.getTypeToken()).isEqualTo(TypeAuthTokenTemp.RESET_PASSWORD);
                    assertThat(foundToken.getUtilisateur().getIdUtilisateur()).isEqualTo(testUser.getIdUtilisateur());
                });

        assertThat(tokenRepository.findByTokenHache("nonExistentHash")).isNotPresent();
    }

    @Test
    void testFindByUtilisateurAndTypeToken() {
        AuthTokenTemporaire token1 = AuthTokenTemporaire.builder()
                .tokenHache("hashForUserAndTypeTest")
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
                .hasValueSatisfying(foundToken -> assertThat(foundToken.getTokenHache()).isEqualTo("hashForUserAndTypeTest"));

        assertThat(tokenRepository.findByUtilisateurAndTypeToken(testUser, TypeAuthTokenTemp.RESET_PASSWORD))
                .isNotPresent();
    }

    @Test
    void testDeleteByDateExpirationBefore() {
        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .tokenHache("expiredTokenForDeleteTest")
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusDays(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(expiredToken);

        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenHache("validTokenForDeleteTest")
                .typeToken(TypeAuthTokenTemp.CONNEXION)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .utilisateur(testUser)
                .isUsed(false)
                .build();
        entityManager.persist(validToken);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        long deletedCount = tokenRepository.deleteByDateExpirationBefore(now);

        assertThat(deletedCount).isEqualTo(1);
        assertThat(tokenRepository.findByTokenHache("expiredTokenForDeleteTest")).isNotPresent();
        assertThat(tokenRepository.findByTokenHache("validTokenForDeleteTest")).isPresent();
    }
}