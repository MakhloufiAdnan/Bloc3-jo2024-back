package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
class OffreRepositoryTest {

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Adresse adresseParis;

    @BeforeEach
    void setUp() {

        Pays paysFrance;
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
                .prix(java.math.BigDecimal.valueOf(60.00))
                .capacite(1)
                .statutOffre(StatutOffre.DISPONIBLE)
                .discipline(disciplineForTest)
                .build();

        Offre savedOffre = offreRepository.save(offre);
        entityManager.flush();
        entityManager.detach(savedOffre); // Important pour s'assurer qu'il est rechargé

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
        LocalDateTime queryNowParam = testRunActualNow.plusDays(60).withNano(0);
        LocalDate queryCurrentDateParam = queryNowParam.toLocalDate();

        Discipline disciplineEffectivelyPast = createAndPersistDiscipline("Disc Past Relative To Query", queryNowParam.minusDays(5));
        Discipline disciplineEffectivelyFuture = createAndPersistDiscipline("Disc Future Relative To Query", queryNowParam.plusDays(5));

        Offre offreToExpireByOwnDate = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(1), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);
        Offre offreToExpireByDisciplineDate = createAndPersistOffre("DUO", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyPast, StatutOffre.DISPONIBLE);
        Offre offreNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, queryNowParam.plusDays(5), disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);
        Offre offreAlreadyExpired = createAndPersistOffre("SOLO", BigDecimal.TEN, queryNowParam.minusDays(10), disciplineEffectivelyPast, StatutOffre.EXPIRE);
        Offre offreNullOwnDateToExpire = createAndPersistOffre("DUO", BigDecimal.TEN, null, disciplineEffectivelyPast, StatutOffre.DISPONIBLE);
        Offre offreNullOwnDateNotToExpire = createAndPersistOffre("FAMILIALE", BigDecimal.TEN, null, disciplineEffectivelyFuture, StatutOffre.DISPONIBLE);
        entityManager.flush();

        int updatedCount = offreRepository.updateStatusForEffectivelyExpiredOffers(queryNowParam, queryCurrentDateParam);
        entityManager.flush();
        entityManager.clear();

        assertEquals(3, updatedCount);

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