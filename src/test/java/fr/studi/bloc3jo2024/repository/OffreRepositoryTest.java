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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat; // Ensure this import for AssertJ
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffreRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_offre_db_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("test_user_offre")
            .withPassword("test_pass_offre");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private TestEntityManager entityManager;

    // These are shared fixtures initialized in setUp and used by helper methods or tests.
    // The IDE warning "Field can be converted to a local variable" can be evaluated,
    // but for shared setup, instance fields are common.
    private Pays paysFrance;
    private Adresse adresseParis;

    @BeforeEach
    void setUp() {
        Optional<Pays> paysOpt = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst();
        if (paysOpt.isPresent()) {
            paysFrance = paysOpt.get();
        } else {
            paysFrance = Pays.builder().nomPays("France").build();
            entityManager.persist(paysFrance);
        }

        adresseParis = Adresse.builder()
                .numeroRue(1)
                .nomRue("Rue Test Offre")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysFrance)
                .build();
        entityManager.persist(adresseParis);
        entityManager.flush();
    }

    private Offre createAndPersistOffre(String type, BigDecimal prix, LocalDateTime dateExpiration, Discipline discipline, StatutOffre statut) {
        Offre offre = Offre.builder()
                .typeOffre(TypeOffre.valueOf(type))
                .prix(prix)
                .quantite(10)
                .capacite(1)
                .dateExpiration(dateExpiration)
                .discipline(discipline)
                .statutOffre(statut)
                .featured(false)
                .build();
        return entityManager.persistAndFlush(offre);
    }

    private Discipline createAndPersistDiscipline(String nom, LocalDateTime dateDiscipline) {
        // The responsibility to provide a 'dateDiscipline' valid at persistence time (not in the actual past)
        // lies with the caller of this method within the test, especially if the Discipline entity
        // has @PrePersist validation against LocalDateTime.now().
        Discipline discipline = Discipline.builder()
                .nomDiscipline(nom)
                .dateDiscipline(dateDiscipline)
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .build();
        return entityManager.persistAndFlush(discipline);
    }


    @Test
    void testSaveAndRetrieveOffre() {
        LocalDateTime futureDisciplineDate = LocalDateTime.now().plusDays(10).withNano(0);
        Discipline disciplineForTest = createAndPersistDiscipline("Natation for Save Test", futureDisciplineDate);

        Offre offre = Offre.builder()
                .typeOffre(TypeOffre.SOLO)
                .quantite(3)
                .prix(java.math.BigDecimal.valueOf(60.00)) // Different price used here
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineForTest)
                .build();

        Offre savedOffre = offreRepository.save(offre);
        entityManager.flush();
        entityManager.detach(savedOffre);

        Optional<Offre> retrievedOffreOpt = offreRepository.findById(savedOffre.getIdOffre());
        assertThat(retrievedOffreOpt).isPresent();
        Offre retrievedOffre = retrievedOffreOpt.get();

        assertNotNull(retrievedOffre);
        assertEquals(disciplineForTest.getIdDiscipline(), retrievedOffre.getDiscipline().getIdDiscipline());
        assertEquals(TypeOffre.SOLO, retrievedOffre.getTypeOffre());
        assertEquals(3, retrievedOffre.getQuantite());
        assertEquals(0, java.math.BigDecimal.valueOf(60.00).compareTo(retrievedOffre.getPrix()));
    }

    @Test
    void updateStatusForEffectivelyExpiredOffers_shouldUpdateCorrectly() {
        LocalDateTime testRunActualNow = LocalDateTime.now();
        LocalDateTime queryNowParam = testRunActualNow.plusDays(60).withNano(0); // Ensure all relative "past" dates are still future to actual now
        LocalDate queryCurrentDateParam = queryNowParam.toLocalDate();

        Discipline disciplineEffectivelyPast = createAndPersistDiscipline("Disc Past Relative To Query", queryNowParam.minusDays(5));
        Discipline disciplineEffectivelyFuture = createAndPersistDiscipline("Disc Future Relative To Query", queryNowParam.plusDays(5));

        // Scenario 1: Offer expires due to its own dateExpiration
        Offre offreToExpireByOwnDate = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(1), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        // Scenario 2: Offer expires due to its discipline's date
        Offre offreToExpireByDisciplineDate = createAndPersistOffre("DUO", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyPast, StatutOffre.DISPONIBLE);

        // Scenario 3: Offer does NOT expire
        Offre offreNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        // Scenario 4: Offer already expired
        Offre offreAlreadyExpired = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(10), disciplineEffectivelyPast, StatutOffre.EXPIRE);

        // Scenario 5: Offer with null dateExpiration, expires due to disciplineEffectivelyPast
        Offre offreNullOwnDateToExpire = createAndPersistOffre("DUO", BigDecimal.TEN, null, disciplineEffectivelyPast, StatutOffre.DISPONIBLE);

        // Scenario 6: Offer with null dateExpiration, does NOT expire (disciplineEffectivelyFuture)
        Offre offreNullOwnDateNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, null, disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        entityManager.flush();

        int updatedCount = offreRepository.updateStatusForEffectivelyExpiredOffers(queryNowParam, queryCurrentDateParam);
        entityManager.flush();
        entityManager.clear();

        assertEquals(3, updatedCount, "Should update 3 offers based on the logic.");

        // Using AssertJ's Optional assertions for cleaner and safer checks
        assertThat(offreRepository.findById(offreToExpireByOwnDate.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.EXPIRE));

        assertThat(offreRepository.findById(offreToExpireByDisciplineDate.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.EXPIRE));

        assertThat(offreRepository.findById(offreNullOwnDateToExpire.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.EXPIRE));

        assertThat(offreRepository.findById(offreNotToExpire.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.DISPONIBLE));

        assertThat(offreRepository.findById(offreAlreadyExpired.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.EXPIRE));

        assertThat(offreRepository.findById(offreNullOwnDateNotToExpire.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.DISPONIBLE));
    }
}