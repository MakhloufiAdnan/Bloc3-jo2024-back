package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

/**
 * Tests d'intégration pour {@link AdresseRepository}.
 * Ces tests utilisent Testcontainers pour démarrer une base de données PostgreSQL réelle
 * afin de vérifier les requêtes JPA et les interactions avec la base de données.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Désactive le remplacement de la datasource par une H2 en mémoire
class AdresseRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db_adresse_repo_" + UUID.randomUUID().toString().substring(0, 8)) // Nom de DB unique
            .withUsername("testuser_addr")
            .withPassword("testpass_addr");

    /**
     * Configure dynamiquement les propriétés de la datasource pour pointer vers le conteneur Testcontainers.
     */
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

    // Données de test communes
    private Pays francePays;
    private Adresse adressePersisted1;
    private Adresse adressePersisted2;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        // Initialisation du rôle USER s'il n'existe pas
        roleUser = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :nom", Role.class)
                .setParameter("nom", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setTypeRole(TypeRole.USER);
                    return entityManager.persistAndFlush(newRole);
                });

        // Initialisation du pays "France" s'il n'existe pas
        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = Pays.builder().nomPays("France").build();
                    return entityManager.persistAndFlush(newPays);
                });

        // Création et persistence des adresses de test
        adressePersisted1 = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .utilisateurs(new HashSet<>()) // Initialisation des collections
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted1);

        adressePersisted2 = Adresse.builder()
                .numeroRue(25)
                .nomRue("Boulevard Haussmann")
                .ville("Paris") // Même ville pour tester findByVille
                .codePostal("75009")
                .pays(francePays)
                .utilisateurs(new HashSet<>())
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted2);
    }

    @Test
    @DisplayName("findByVille doit retourner les adresses correspondantes")
    void findByVille_shouldReturnMatchingAdresses() {
        // Act
        List<Adresse> adressesParis = adresseRepository.findByVille("Paris");

        // Assert
        assertNotNull(adressesParis, "La liste ne doit pas être null.");
        assertEquals(2, adressesParis.size(), "Doit trouver 2 adresses à Paris.");
    }

    @Test
    @DisplayName("findByVille doit retourner une liste vide si aucune adresse ne correspond")
    void findByVille_shouldReturnEmptyListWhenNoMatch() {
        // Act
        List<Adresse> adressesInconnues = adresseRepository.findByVille("VilleInconnue");

        // Assert
        assertNotNull(adressesInconnues, "La liste ne doit pas être null.");
        assertTrue(adressesInconnues.isEmpty(), "La liste doit être vide.");
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner l'adresse si elle existe")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnAdresseWhenExists() {
        // Act
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                10, "Rue de la Paix", "Paris", "75001", francePays
        );

        // Assert
        assertTrue(found.isPresent(), "L'adresse doit être trouvée.");
        assertEquals(adressePersisted1.getIdAdresse(), found.get().getIdAdresse(), "L'ID de l'adresse trouvée doit correspondre.");
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner Optional vide si l'adresse n'existe pas")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnEmptyWhenNotExists() {
        // Act
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                999, "Rue Inconnue", "VilleInconnue", "00000", francePays
        );

        // Assert
        assertFalse(found.isPresent(), "Aucune adresse ne doit être trouvée.");
    }


    @Test
    @DisplayName("findByUtilisateurs_IdUtilisateur doit retourner les adresses pour un utilisateur")
    void findByUtilisateurs_IdUtilisateur_shouldReturnAdressesForUser() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder()
                .email("user-" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
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

        Utilisateur persistedUtilisateur = entityManager.persistAndFlush(utilisateur);

        // Act
        List<Adresse> adressesTrouvees = adresseRepository.findByUtilisateurs_IdUtilisateur(persistedUtilisateur.getIdUtilisateur());

        // Assert
        assertNotNull(adressesTrouvees, "La liste ne doit pas être null.");
        assertFalse(adressesTrouvees.isEmpty(), "Devrait trouver au moins une adresse pour cet utilisateur.");
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse(), "L'adresse trouvée doit être celle de l'utilisateur.");
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner true si l'adresse est liée")
    void isAdresseLieeAUnDiscipline_shouldReturnTrueWhenLinked() {
        // Arrange: Créer une discipline et la lier à adressePersisted1
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Escrime")
                .dateDiscipline(LocalDateTime.now().plusDays(30))
                .nbPlaceDispo(50)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        // Act
        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted1.getIdAdresse());

        // Assert
        assertTrue(isLinked, "L'adresse doit être marquée comme liée.");
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner false si l'adresse n'est pas liée")
    void isAdresseLieeAUnDiscipline_shouldReturnFalseWhenNotLinked() {
        // Act: adressePersisted2 n'a pas de discipline liée dans le setup
        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted2.getIdAdresse());

        // Assert
        assertFalse(isLinked, "L'adresse ne doit pas être marquée comme liée.");
    }

    @Test
    @DisplayName("findByDisciplinesAndPays_IdPays doit retourner les adresses correspondantes")
    void findByDisciplinesAndPays_IdPays_shouldReturnMatchingAdresses() {
        // Arrange: Créer une discipline liée à adressePersisted1 (qui est en France)
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Judo")
                .dateDiscipline(LocalDateTime.now().plusMonths(2))
                .nbPlaceDispo(30)
                .adresse(adressePersisted1) // Liaison à adressePersisted1
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        // Act
        List<Adresse> result = adresseRepository.findByDisciplinesAndPays_IdPays(discipline, francePays.getIdPays());

        // Assert
        assertNotNull(result, "La liste ne doit pas être null.");
        assertFalse(result.isEmpty(), "La liste ne doit pas être vide.");
        assertEquals(adressePersisted1.getIdAdresse(), result.getFirst().getIdAdresse(), "L'adresse trouvée doit être adressePersisted1.");
    }

    @Test
    @DisplayName("findByDisciplines doit retourner l'adresse associée à la discipline")
    void findByDisciplines_shouldReturnAdresseForDiscipline() {
        // Arrange
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Tir à l'arc")
                .dateDiscipline(LocalDateTime.now().plusDays(60))
                .nbPlaceDispo(20)
                .adresse(adressePersisted2) // Discipline liée à adressePersisted2
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        // Act
        Optional<Adresse> foundAdresseOpt = adresseRepository.findByDisciplines(discipline);

        // Assert
        assertTrue(foundAdresseOpt.isPresent(), "Une adresse doit être trouvée pour la discipline.");
        assertEquals(adressePersisted2.getIdAdresse(), foundAdresseOpt.get().getIdAdresse(), "L'ID de l'adresse doit correspondre à celui de adressePersisted2.");
    }

    @Test
    @DisplayName("findByDisciplinesContaining doit retourner une liste d'adresses pour la discipline")
    void findByDisciplinesContaining_shouldReturnListOfAdressesForDiscipline() {
        // Arrange
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Natation Synchronisée")
                .dateDiscipline(LocalDateTime.now().plusDays(90))
                .nbPlaceDispo(15)
                .adresse(adressePersisted1) // Discipline liée à adressePersisted1
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        // Act
        List<Adresse> adressesTrouvees = adresseRepository.findByDisciplinesContaining(discipline);

        // Assert
        assertNotNull(adressesTrouvees);
        assertFalse(adressesTrouvees.isEmpty(), "La liste ne doit pas être vide.");
        assertEquals(1, adressesTrouvees.size(), "Doit trouver une seule adresse pour cette discipline.");
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse());
    }
}