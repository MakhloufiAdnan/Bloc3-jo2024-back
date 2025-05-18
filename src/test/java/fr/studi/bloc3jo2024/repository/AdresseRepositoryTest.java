package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Pays;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdresseRepositoryTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
            .withDatabaseName("test_adresse_repo_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_addr_repo")
            .withPassword("test_pass_addr_repo");

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
    private Adresse testAdresse;

    @BeforeEach
    void setUp() {
        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = new Pays();
                    newPays.setNomPays("France");
                    return entityManager.persist(newPays); // Utilisation de persist ici aussi
                });
        entityManager.flush(); // S'assurer que pays a un ID si créé

        // Créer une instance d'Adresse mais ne pas la persister ici,
        // la persistance se fera dans chaque méthode de test si nécessaire.
        testAdresse = new Adresse();
        testAdresse.setNumeroRue(123);
        testAdresse.setNomRue("Rue Test AdresseRepo");
        testAdresse.setVille("Paris");
        testAdresse.setCodePostal("75000");
        testAdresse.setPays(francePays);
    }

    @Test
    void testSaveAdresse() {
        Adresse savedAdresse = adresseRepository.save(testAdresse); // testAdresse est maintenant persistée
        entityManager.flush(); // Important pour générer l'ID et pour la visibilité

        assertNotNull(savedAdresse, "L'adresse sauvegardée ne doit pas être null.");
        assertNotNull(savedAdresse.getIdAdresse(), "L'ID de l'adresse sauvegardée ne doit pas être null.");
        assertEquals(123, savedAdresse.getNumeroRue());
        assertEquals("Rue Test AdresseRepo", savedAdresse.getNomRue());
        assertEquals("Paris", savedAdresse.getVille());
        assertEquals("75000", savedAdresse.getCodePostal());
        assertNotNull(savedAdresse.getPays(), "Le pays associé ne doit pas être null.");
        assertEquals(francePays.getIdPays(), savedAdresse.getPays().getIdPays(), "L'ID du pays associé doit correspondre.");
    }

    @Test
    void testFindById() {
        // Persister explicitement l'adresse pour ce test
        entityManager.persist(testAdresse);
        entityManager.flush();

        Optional<Adresse> foundOptional = adresseRepository.findById(testAdresse.getIdAdresse());
        assertTrue(foundOptional.isPresent(), "L'adresse devrait être trouvée par son ID.");
        Adresse foundAdresse = foundOptional.get();

        assertEquals("Paris", foundAdresse.getVille());
        assertNotNull(foundAdresse.getPays(), "Le pays de l'adresse trouvée ne doit pas être null.");
        assertEquals(francePays.getIdPays(), foundAdresse.getPays().getIdPays(), "L'ID du pays doit correspondre.");
    }
}