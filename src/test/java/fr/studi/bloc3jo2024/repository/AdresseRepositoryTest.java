package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.Role;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdresseRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db_adresse_repo_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("testuser_addr")
            .withPassword("testpass_addr");

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
    private AdresseRepository adresseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Pays francePays;
    private Adresse adressePersisted1;
    private Adresse adressePersisted2;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :nom", Role.class)
                .setParameter("nom", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setTypeRole(TypeRole.USER);
                    return entityManager.persistAndFlush(newRole);
                });


        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = Pays.builder().nomPays("France").build();
                    return entityManager.persistAndFlush(newPays);
                });

        adressePersisted1 = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .utilisateurs(new HashSet<>())
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted1);

        adressePersisted2 = Adresse.builder()
                .numeroRue(25)
                .nomRue("Boulevard Haussmann")
                .ville("Paris")
                .codePostal("75009")
                .pays(francePays)
                .utilisateurs(new HashSet<>())
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted2);
    }

    @Test
    void findByVille_shouldReturnMatchingAdresses() {
        List<Adresse> adressesParis = adresseRepository.findByVille("Paris");
        assertNotNull(adressesParis);
        assertEquals(2, adressesParis.size());
    }

    @Test
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnAdresseWhenExists() {
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                10, "Rue de la Paix", "Paris", "75001", francePays
        );
        assertTrue(found.isPresent());
        assertEquals(adressePersisted1.getIdAdresse(), found.get().getIdAdresse());
    }

    @Test
    void findByUtilisateurs_IdUtilisateur_shouldReturnAdressesForUser() {
        Utilisateur utilisateur = Utilisateur.builder()
                .idUtilisateur(UUID.randomUUID())
                .email("user-" + UUID.randomUUID() + "@example.com")
                .nom("UserNom")
                .prenom("UserPrenom")
                .dateNaissance(LocalDate.now().minusYears(25))
                .adresse(adressePersisted1)
                .role(roleUser)
                .telephones(new ArrayList<>())
                .authTokensTemporaires(new ArrayList<>())
                .paniers(new ArrayList<>())
                .billets(new ArrayList<>())
                .build();
        entityManager.persistAndFlush(utilisateur);

        List<Adresse> adressesTrouvees = adresseRepository.findByUtilisateurs_IdUtilisateur(utilisateur.getIdUtilisateur());

        assertNotNull(adressesTrouvees);
        assertFalse(adressesTrouvees.isEmpty(), "Devrait trouver au moins une adresse pour cet utilisateur");
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse());
    }

    @Test
    void isAdresseLieeAUnDiscipline_shouldReturnTrueWhenLinked() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Escrime")
                .dateDiscipline(LocalDateTime.now().plusDays(30))
                .nbPlaceDispo(50)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporte(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted1.getIdAdresse());
        assertTrue(isLinked);
    }

    @Test
    void isAdresseLieeAUnDiscipline_shouldReturnFalseWhenNotLinked() {
        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted2.getIdAdresse());
        assertFalse(isLinked);
    }

    @Test
    void findByDisciplinesAndPays_IdPays_shouldReturnMatchingAdresses() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Judo")
                .dateDiscipline(LocalDateTime.now().plusMonths(2))
                .nbPlaceDispo(30)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporte(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        List<Adresse> result = adresseRepository.findByDisciplinesAndPays_IdPays(discipline, francePays.getIdPays());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(adressePersisted1.getIdAdresse(), result.getFirst().getIdAdresse());
    }
}
