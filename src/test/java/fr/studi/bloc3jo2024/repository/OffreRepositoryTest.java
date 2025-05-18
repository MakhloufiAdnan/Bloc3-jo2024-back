package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffreRepositoryTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
            .withDatabaseName("test_offre_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_offre")
            .withPassword("test_pass_offre");

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
    private OffreRepository offreRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Discipline disciplineEntity;

    @BeforeEach
    void setUp() {
        Pays pays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = new Pays();
                    newPays.setNomPays("France");
                    return entityManager.persist(newPays);
                });

        Adresse adresse = Adresse.builder()
                .numeroRue(5)
                .nomRue("Rue des Athlètes Offre")
                .ville("Paris")
                .codePostal("75001")
                .pays(pays)
                .build();
        entityManager.persist(adresse);

        disciplineEntity = Discipline.builder()
                .nomDiscipline("Natation Test Offre")
                .dateDiscipline(LocalDateTime.now().plusDays(7))
                .nbPlaceDispo(100)
                .adresse(adresse)
                .build();
        entityManager.persist(disciplineEntity);
        entityManager.flush();
    }

    @Test
    void testSaveAndRetrieveOffre() {
        // Créer Offre - Supposant que votre entité Offre a un constructeur ou des setters
        Offre offre = new Offre(); // Si @Builder n'est pas disponible
        offre.setTypeOffre(TypeOffre.SOLO);
        offre.setQuantite(3);
        offre.setPrix(java.math.BigDecimal.valueOf(60.00));
        offre.setCapacite(1);
        offre.setStatutOffre(StatutOffre.DISPONIBLE);
        offre.setDiscipline(disciplineEntity);

        Offre savedOffre = offreRepository.save(offre);
        entityManager.flush();

        Offre retrievedOffre = offreRepository.findById(savedOffre.getIdOffre()).orElse(null);

        assertNotNull(retrievedOffre, "L'offre récupérée ne devrait pas être null.");
        assertNotNull(retrievedOffre.getIdOffre(), "L'ID de l'offre ne devrait pas être null après sauvegarde.");
        assertEquals(TypeOffre.SOLO, retrievedOffre.getTypeOffre());
        assertEquals(3, retrievedOffre.getQuantite());
        assertEquals(0, java.math.BigDecimal.valueOf(60.00).compareTo(retrievedOffre.getPrix()), "Les prix devraient correspondre.");
        assertEquals(1, retrievedOffre.getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, retrievedOffre.getStatutOffre());
        assertNotNull(retrievedOffre.getDiscipline(), "La discipline associée ne devrait pas être null.");
        assertEquals(disciplineEntity.getIdDiscipline(), retrievedOffre.getDiscipline().getIdDiscipline(), "L'ID de la discipline associée devrait correspondre.");
    }
}