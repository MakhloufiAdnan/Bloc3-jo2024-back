package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DisciplineRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DisciplineRepository disciplineRepository;

    private Pays paysFrance;
    private Adresse adresseParis;
    private Adresse adresseLyon;
    private Epreuve epreuveNatationCourse;
    private Epreuve epreuveJudoCombat;
    private Discipline disciplineNatationSynchro;
    private Discipline disciplineJudoKata;
    private Discipline disciplineEscrimeFleuret;

    private <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @BeforeEach
    void setUp() {

        entityManager.createNativeQuery("DELETE FROM comporter").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM offres").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM disciplines").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM epreuves").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM adresses").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM pays").executeUpdate();
        entityManager.flush();

        paysFrance = persistAndFlush(Pays.builder().nomPays("FranceRepoTest").build());

        adresseParis = persistAndFlush(Adresse.builder().numeroRue(10).nomRue("Rue de Paris").ville("Paris").codePostal("75001").pays(paysFrance).build());

        adresseLyon = persistAndFlush(Adresse.builder().numeroRue(20).nomRue("Rue de Lyon").ville("Lyon").codePostal("69001").pays(paysFrance).build());

        epreuveNatationCourse = persistAndFlush(Epreuve.builder().nomEpreuve("Natation 100m Nage Libre").isFeatured(true).build());
        epreuveJudoCombat = persistAndFlush(Epreuve.builder().nomEpreuve("Judo Combat -70kg").isFeatured(false).build());

        disciplineNatationSynchro = persistAndFlush(Discipline.builder()
                .nomDiscipline("Natation Synchronisée")
                .dateDiscipline(LocalDateTime.now().plusDays(5))
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .offres(new HashSet<>())
                .build());

        disciplineJudoKata = persistAndFlush(Discipline.builder()
                .nomDiscipline("Judo Kata")
                .dateDiscipline(LocalDateTime.now().plusDays(15))
                .nbPlaceDispo(50)
                .adresse(adresseLyon)
                .comporters(new HashSet<>())
                .offres(new HashSet<>())
                .build());

        disciplineEscrimeFleuret = persistAndFlush(Discipline.builder()
                .nomDiscipline("Escrime Fleuret")
                .dateDiscipline(LocalDateTime.now().plusSeconds(10))
                .nbPlaceDispo(20)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .offres(new HashSet<>())
                .build());

        Comporter comporterNatation = Comporter.builder()
                .id(new ComporterKey(disciplineNatationSynchro.getIdDiscipline(), epreuveNatationCourse.getIdEpreuve()))
                .discipline(disciplineNatationSynchro)
                .epreuve(epreuveNatationCourse)
                .jrDeMedaille(true)
                .build();
        persistAndFlush(comporterNatation);

        disciplineNatationSynchro.getComporters().add(comporterNatation);
        epreuveNatationCourse.getComporters().add(comporterNatation);

        Comporter comporterJudo = Comporter.builder()
                .id(new ComporterKey(disciplineJudoKata.getIdDiscipline(), epreuveJudoCombat.getIdEpreuve()))
                .discipline(disciplineJudoKata)
                .epreuve(epreuveJudoCombat)
                .jrDeMedaille(false)
                .build();
        persistAndFlush(comporterJudo);
        disciplineJudoKata.getComporters().add(comporterJudo);
        epreuveJudoCombat.getComporters().add(comporterJudo);

        Comporter comporterNatationSurJudoEpreuve = Comporter.builder()
                .id(new ComporterKey(disciplineNatationSynchro.getIdDiscipline(), epreuveJudoCombat.getIdEpreuve()))
                .discipline(disciplineNatationSynchro)
                .epreuve(epreuveJudoCombat)
                .jrDeMedaille(false)
                .build();
        persistAndFlush(comporterNatationSurJudoEpreuve);
        disciplineNatationSynchro.getComporters().add(comporterNatationSurJudoEpreuve);
        epreuveJudoCombat.getComporters().add(comporterNatationSurJudoEpreuve);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findFutureDisciplinesWithAdresse_shouldReturnPagedFutureDisciplines() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime nowReference = LocalDateTime.now().plusSeconds(30);

        Page<Discipline> resultPage = disciplineRepository.findFutureDisciplinesWithAdresse(nowReference, pageable);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent())
                .hasSize(2)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Judo Kata");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void decrementerPlaces_shouldDecreasePlaces_whenSufficientPlaces() {
        int placesToDecrement = 10;
        Long disciplineId = disciplineNatationSynchro.getIdDiscipline();

        Discipline disciplineToUpdate = disciplineRepository.findById(disciplineId).orElseThrow();
        int initialPlaces = disciplineToUpdate.getNbPlaceDispo();

        int updatedRows = disciplineRepository.decrementerPlaces(disciplineId, placesToDecrement);
        assertThat(updatedRows).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Discipline updatedDiscipline = disciplineRepository.findById(disciplineId).orElseThrow();
        assertThat(updatedDiscipline.getNbPlaceDispo()).isEqualTo(initialPlaces - placesToDecrement);
    }

    @Test
    void decrementerPlaces_shouldNotDecreasePlaces_whenInsufficientPlaces() {
        int placesToDecrement = disciplineJudoKata.getNbPlaceDispo() + 1;
        Long disciplineId = disciplineJudoKata.getIdDiscipline();

        Discipline disciplineToUpdate = disciplineRepository.findById(disciplineId).orElseThrow();
        int initialPlaces = disciplineToUpdate.getNbPlaceDispo();

        int updatedRows = disciplineRepository.decrementerPlaces(disciplineId, placesToDecrement);
        assertThat(updatedRows).isZero();

        entityManager.flush();
        entityManager.clear();

        Discipline nonUpdatedDiscipline = disciplineRepository.findById(disciplineId).orElseThrow();
        assertThat(nonUpdatedDiscipline.getNbPlaceDispo()).isEqualTo(initialPlaces);
    }

    @Test
    void findDisciplinesByVilleWithAdresse_shouldReturnPagedDisciplinesForGivenVille() {
        Pageable pageable = PageRequest.of(0, 10);
        String ville = "Paris";

        Page<Discipline> resultPage = disciplineRepository.findDisciplinesByVilleWithAdresse(ville, pageable);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent())
                .hasSize(2)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Escrime Fleuret");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdWithAdresse_shouldReturnPagedDisciplinesForGivenEpreuveId() {
        Long epreuveId = epreuveNatationCourse.getIdEpreuve(); // Linked only to Natation Synchronisée
        Pageable pageable = PageRequest.of(0, 10);

        Page<Discipline> resultPage = disciplineRepository.findDisciplinesByEpreuveIdWithAdresse(epreuveId, pageable);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().getFirst().getNomDiscipline()).isEqualTo("Natation Synchronisée");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdsWithAdresse_shouldReturnSetOfDisciplinesForGivenEpreuveIds() {
        List<Long> epreuveIds = List.of(epreuveNatationCourse.getIdEpreuve(), epreuveJudoCombat.getIdEpreuve());

        Set<Discipline> resultSet = disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(epreuveIds);

        assertThat(resultSet).isNotNull();
        assertThat(resultSet)
                .hasSize(2) // Natation Synchronisée (via Natation AND Judo) and Judo Kata (via Judo)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Judo Kata");
        Optional<Discipline> natationOpt = resultSet.stream().filter(d -> d.getNomDiscipline().equals("Natation Synchronisée")).findFirst();
        assertThat(natationOpt).isPresent();
        assertThat(natationOpt.get().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdsWithAdresse_shouldReturnEmptySet_whenNoMatchingEpreuveIds() {
        List<Long> epreuveIdsNonExistants = List.of(999L, 888L);
        Set<Discipline> resultSet = disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(epreuveIdsNonExistants);
        assertThat(resultSet).isNotNull().isEmpty();
    }

    @Test
    void findAllWithAdresse_shouldReturnAllDisciplinesWithAdresse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Discipline> resultPage = disciplineRepository.findAllWithAdresse(pageable);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(3); // 3 disciplines created in setUp
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByDateDiscipline_shouldReturnMatchingDisciplines() {
        // Use one of the dates from setUp, for example disciplineJudoKata's date
        LocalDateTime targetDate = disciplineJudoKata.getDateDiscipline();
        List<Discipline> resultList = disciplineRepository.findDisciplinesByDateDiscipline(targetDate);

        assertThat(resultList)
                .hasSize(1)
                .first()
                .extracting(Discipline::getNomDiscipline).isEqualTo("Judo Kata");
    }
}