package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthTokenTemporaireRepositoryTest {

    @Autowired
    private AuthTokenTemporaireRepository tokenRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur createUser(String email) {
        Pays pays = new Pays();
        pays.setNomPays("France");
        entityManager.persist(pays);

        Adresse adresse = Adresse.builder()
                .nomRue("rue test")
                .numeroRue(10)
                .ville("Paris")
                .codePostal("75000")
                .pays(pays)
                .build();
        entityManager.persist(adresse);

        Role role = Role.builder()
                .typeRole(TypeRole.USER)
                .build();
        entityManager.persist(role);

        Utilisateur user = Utilisateur.builder()
                .email(email)
                .nom("TestNom")
                .prenom("TestPrenom")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(role)
                .adresse(adresse)
                .build();
        entityManager.persist(user);
        return user;
    }

    @Test
    @Transactional
    void testCreationTokenTemporaire() {
        Utilisateur user = createUser("token@jo.fr");

        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
                .tokenHache("hashed123")
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusDays(1))
                .utilisateur(user)
                .build();

        AuthTokenTemporaire savedToken = tokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        Optional<AuthTokenTemporaire> retrievedToken = tokenRepository.findById(savedToken.getIdTokenTemp());
        assertThat(retrievedToken).isPresent();
        retrievedToken.ifPresent(t -> {
            assertThat(t.getIdTokenTemp()).isNotNull();
            assertThat(t.getTypeToken()).isEqualTo(TypeAuthTokenTemp.VALIDATION_EMAIL);
            assertThat(t.getTokenHache()).isEqualTo("hashed123");
            assertThat(t.getDateExpiration()).isAfter(LocalDateTime.now());
            assertThat(t.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
            Utilisateur retrievedUser = entityManager.find(Utilisateur.class, user.getIdUtilisateur());
            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.getAuthTokensTemporaires()).contains(t);
        });
    }

    @Test
    @Transactional
    void testFindByTokenHache() {
        Utilisateur user = createUser("findby@jo.fr");

        AuthTokenTemporaire token = AuthTokenTemporaire.builder()
                .tokenHache("uniqueHashed")
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(2))
                .utilisateur(user)
                .build();
        AuthTokenTemporaire savedToken = tokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        Optional<AuthTokenTemporaire> foundTokenOptional = tokenRepository.findByTokenHache("uniqueHashed");
        assertThat(foundTokenOptional)
                .isPresent()
                .hasValueSatisfying(foundToken -> {
                    assertThat(foundToken.getIdTokenTemp()).isEqualTo(savedToken.getIdTokenTemp());
                    assertThat(foundToken.getTypeToken()).isEqualTo(TypeAuthTokenTemp.RESET_PASSWORD);
                    assertThat(foundToken.getUtilisateur().getIdUtilisateur()).isEqualTo(user.getIdUtilisateur());
                });

        assertThat(tokenRepository.findByTokenHache("nonExistentHash")).isNotPresent();
    }

    @Test
    @Transactional
    void testFindByUtilisateurAndTypeToken() {
        Utilisateur user1 = createUser("user1@jo.fr");

        AuthTokenTemporaire token1 = AuthTokenTemporaire.builder()
                .tokenHache("hash1")
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusDays(1))
                .utilisateur(user1)
                .build();
        tokenRepository.save(token1);
        entityManager.flush();
        entityManager.clear();

        Optional<AuthTokenTemporaire> foundTokenOptional = tokenRepository.findByUtilisateurAndTypeToken(user1, TypeAuthTokenTemp.VALIDATION_EMAIL);
        assertThat(foundTokenOptional)
                .isPresent()
                .hasValueSatisfying(foundToken -> assertThat(foundToken.getIdTokenTemp()).isEqualTo(token1.getIdTokenTemp()));

        assertThat(tokenRepository.findByUtilisateurAndTypeToken(user1, TypeAuthTokenTemp.RESET_PASSWORD))
                .isNotPresent();
    }

    @Test
    @Transactional
    void testDeleteByDateExpirationBefore() {
        Utilisateur user = createUser("expired@jo.fr");

        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .tokenHache("expired")
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().minusDays(1))
                .utilisateur(user)
                .build();
        tokenRepository.save(expiredToken);

        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenHache("valid")
                .typeToken(TypeAuthTokenTemp.CONNEXION)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .utilisateur(user)
                .build();
        tokenRepository.save(validToken);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime now = LocalDateTime.now();
        long deletedCount = tokenRepository.deleteByDateExpirationBefore(now);

        assertThat(deletedCount).isEqualTo(1);
        assertThat(tokenRepository.findById(expiredToken.getIdTokenTemp())).isNotPresent();
        assertThat(tokenRepository.findById(validToken.getIdTokenTemp())).isPresent();
    }
}