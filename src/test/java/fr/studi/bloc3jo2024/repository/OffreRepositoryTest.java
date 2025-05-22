package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OffreRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Adresse adresseParis;

    /**
     * Nettoie les données et prépare les entités de base pour les tests.
     */
    @BeforeEach
    void setUp() {

        Pays paysFrance;
        entityManager.getEntityManager().createQuery("DELETE FROM Offre").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Discipline").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();
        entityManager.flush();
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

    /**
     * Méthode d'aide pour créer et persister une entité Offre.
     * @param type Le type d'offre.
     * @param prix Le prix de l'offre.
     * @param dateExpiration La date d'expiration de l'offre.
     * @param discipline La discipline associée à l'offre.
     * @param statut Le statut de l'offre.
     * @return L'entité Offre persistée.
     */
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

    /**
     * Méthode d'aide pour créer et persister une entité Discipline.
     * @param nom Le nom de la discipline.
     * @param dateDiscipline La date de la discipline.
     * @return L'entité Discipline persistée.
     */
    private Discipline createAndPersistDiscipline(String nom, LocalDateTime dateDiscipline) {
        Discipline discipline = Discipline.builder()
                .nomDiscipline(nom)
                .dateDiscipline(dateDiscipline)
                .nbPlaceDispo(100)
                .adresse(adresseParis) // Utilise l'adresse de test commune
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
                .prix(java.math.BigDecimal.valueOf(60.00))
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineForTest)
                .build();

        Offre savedOffre = offreRepository.save(offre);
        entityManager.flush();
        entityManager.detach(savedOffre); // Détache l'entité pour s'assurer qu'elle est rechargée de la base

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
        // Utiliser une date paramètre future par rapport à 'now' pour simuler le temps qui passe dans la logique métier
        LocalDateTime queryNowParam = testRunActualNow.plusDays(60).withNano(0);
        LocalDate queryCurrentDateParam = queryNowParam.toLocalDate();

        Discipline disciplineEffectivelyPast = createAndPersistDiscipline("Disc Past Relative To Query", queryNowParam.minusDays(5));
        Discipline disciplineEffectivelyFuture = createAndPersistDiscipline("Disc Future Relative To Query", queryNowParam.plusDays(5));

        // Scenario 1: Offer expires due to its own dateExpiration (avant queryNowParam)
        Offre offreToExpireByOwnDate = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(1), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        // Scenario 2: Offer expires due to its discipline's date (disciplineEffectivelyPast)
        Offre offreToExpireByDisciplineDate = createAndPersistOffre("DUO", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyPast, StatutOffre.DISPONIBLE);

        // Scenario 3: Offer does NOT expire (dates futures)
        Offre offreNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        // Scenario 4: Offer already expired (devrait rester EXPIRE et ne pas compter comme mis à jour)
        Offre offreAlreadyExpired = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(10), disciplineEffectivelyPast, StatutOffre.EXPIRE);

        // Scenario 5: Offer with null dateExpiration, expires due to disciplineEffectivelyPast
        Offre offreNullOwnDateToExpire = createAndPersistOffre("DUO", BigDecimal.TEN, null, disciplineEffectivelyPast, StatutOffre.DISPONIBLE);

        // Scenario 6: Offer with null dateExpiration, does NOT expire (disciplineEffectivelyFuture)
        Offre offreNullOwnDateNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, null, disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);

        entityManager.flush();

        // Exécute la méthode à tester
        int updatedCount = offreRepository.updateStatusForEffectivelyExpiredOffers(queryNowParam, queryCurrentDateParam);
        entityManager.flush();
        entityManager.clear();

        // Assertions
        // On s'attend à 3 mises à jour : offreToExpireByOwnDate, offreToExpireByDisciplineDate, offreNullOwnDateToExpire
        assertEquals(3, updatedCount, "Should update 3 offers based on the logic.");

        // Vérification de l'état des offres après l'appel
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
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.EXPIRE)); // Reste EXPIRE

        assertThat(offreRepository.findById(offreNullOwnDateNotToExpire.getIdOffre()))
                .isPresent()
                .hasValueSatisfying(offre -> assertThat(offre.getStatutOffre()).isEqualTo(StatutOffre.DISPONIBLE));
    }
}