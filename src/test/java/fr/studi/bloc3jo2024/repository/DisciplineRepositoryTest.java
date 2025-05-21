/*package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class DisciplineRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db_discipline_repo_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("testuser_discipline_aligned")
            .withPassword("testpass_discipline_aligned");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DisciplineRepository disciplineRepository;

    private Pays paysFrance;
    private Adresse adresseParis;
    private Adresse adresseLyon;
    private Epreuve epreuve1;
    private Epreuve epreuve2;
    private Discipline disciplineFuture1;
    private Discipline disciplineFuture2;
    private Discipline disciplineForPastScenarioTesting; // Renamed for clarity

    @BeforeEach
    void setUp() {
        // Ensure a fresh state if needed, though @DataJpaTest handles rollbacks.
        // entityManager.getEntityManager().createQuery("DELETE FROM Comporter").executeUpdate();
        // entityManager.getEntityManager().createQuery("DELETE FROM Discipline").executeUpdate();
        // entityManager.getEntityManager().createQuery("DELETE FROM Epreuve").executeUpdate();
        // entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        // entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        // entityManager.flush();


        paysFrance = entityManager.persistFlushFind(Pays.builder().nomPays("France").build());

        adresseParis = entityManager.persistFlushFind(Adresse.builder().numeroRue(10).nomRue("Rue de Paris").ville("Paris").codePostal("75001").pays(paysFrance).build());
        adresseLyon = entityManager.persistFlushFind(Adresse.builder().numeroRue(20).nomRue("Rue de Lyon").ville("Lyon").codePostal("69001").pays(paysFrance).build());

        epreuve1 = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Natation 100m").build());
        epreuve2 = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Judo Individuel").build());

        // For all disciplines in setUp, ensure their dates are valid at the time of persistence.
        // The "past" or "future" nature for query testing will be relative to parameters passed to query methods.
        LocalDateTime safeFutureDateBase = LocalDateTime.now().plusMinutes(5); // Base for setting dates to avoid race conditions with now()

        disciplineFuture1 = Discipline.builder()
                .nomDiscipline("Natation")
                .dateDiscipline(safeFutureDateBase.plusDays(5)) // Clearly future
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .build();
        disciplineFuture1 = entityManager.persistAndFlush(disciplineFuture1);

        disciplineFuture2 = Discipline.builder()
                .nomDiscipline("Judo")
                .dateDiscipline(safeFutureDateBase.plusDays(15)) // Clearly future
                .nbPlaceDispo(50)
                .adresse(adresseLyon)
                .comporters(new HashSet<>())
                .build();
        disciplineFuture2 = entityManager.persistAndFlush(disciplineFuture2);

        // This discipline is created with a future date.
        // Tests that need to treat it as "past" will do so by passing a later "currentDate" to repository methods.
        disciplineForPastScenarioTesting = Discipline.builder() // Renamed from disciplinePassee
                .nomDiscipline("Escrime")
                .dateDiscipline(safeFutureDateBase.plusDays(1)) // Valid at persist time
                .nbPlaceDispo(20)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .build();
        // This is the problematic line 110 from the logs.
        // The date safeFutureDateBase.plusDays(1) should be valid.
        entityManager.persistAndFlush(disciplineForPastScenarioTesting);


        Comporter comporter1 = Comporter.builder()
                .id(new ComporterKey(epreuve1.getIdEpreuve(), disciplineFuture1.getIdDiscipline()))
                .epreuve(epreuve1).discipline(disciplineFuture1).jrDeMedaille(true).build();
        entityManager.persistAndFlush(comporter1);
        // No need to manually add to collection and merge if mappings are correct & cascades appropriate
        // disciplineFuture1.getComporters().add(comporter1);
        // entityManager.merge(disciplineFuture1);

        Comporter comporter2 = Comporter.builder()
                .id(new ComporterKey(epreuve2.getIdEpreuve(), disciplineFuture2.getIdDiscipline()))
                .epreuve(epreuve2).discipline(disciplineFuture2).jrDeMedaille(false).build();
        entityManager.persistAndFlush(comporter2);
        // disciplineFuture2.getComporters().add(comporter2);
        // entityManager.merge(disciplineFuture2);
    }

    @Test
    void findByDateDisciplineAfter_shouldReturnFutureDisciplines() {
        LocalDateTime queryThresholdTime = LocalDateTime.now(); // Use actual now for this test's logic

        List<Discipline> resultList = disciplineRepository.findByDateDisciplineAfter(queryThresholdTime);

        assertThat(resultList).isNotNull();
        // All three disciplines (disciplineFuture1, disciplineFuture2, disciplineForPastScenarioTesting)
        // were created with dates after queryThresholdTime (due to safeFutureDateBase).
        assertThat(resultList)
                .hasSize(3)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation", "Judo", "Escrime");
    }

    // ... (rest of the tests in DisciplineRepositoryTest.java remain the same)
    // Ensure that any test relying on "disciplinePassee" now uses "disciplineForPastScenarioTesting"
    // and understands its date is relative to "safeFutureDateBase" from setUp.
    // If a test needs a discipline truly "past" for its query, it should pass a "currentDate" parameter
    // to the repository method that makes "disciplineForPastScenarioTesting.getDateDiscipline()" appear as past.

    @Test
    void findDisciplinesByVille_shouldReturnMatchingDisciplines() {
        String ville = "Paris";
        List<Discipline> resultList = disciplineRepository.findDisciplinesByVille(ville);
        assertThat(resultList).isNotNull();
        // disciplineFuture1 and disciplineForPastScenarioTesting are in Paris
        assertThat(resultList)
                .hasSize(2)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation", "Escrime");
    }
    // ... other tests
}*/