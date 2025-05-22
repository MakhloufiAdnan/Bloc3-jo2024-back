/*package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DisciplineRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

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

    @BeforeEach
    void setUp() {
        entityManager.getEntityManager().createQuery("DELETE FROM Comporter").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Offre").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Discipline").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Epreuve").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Adresse").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Pays").executeUpdate();

        paysFrance = entityManager.persistFlushFind(Pays.builder().nomPays("France").build());

        adresseParis = entityManager.persistFlushFind(Adresse.builder().numeroRue(10).nomRue("Rue de Paris").ville("Paris").codePostal("75001").pays(paysFrance).build());
        adresseLyon = entityManager.persistFlushFind(Adresse.builder().numeroRue(20).nomRue("Rue de Lyon").ville("Lyon").codePostal("69001").pays(paysFrance).build());

        epreuveNatationCourse = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Natation 100m Nage Libre").isFeatured(true).build());
        epreuveJudoCombat = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Judo Combat -70kg").isFeatured(false).build());

        // Création des disciplines
        disciplineNatationSynchro = Discipline.builder()
                .nomDiscipline("Natation Synchronisée")
                .dateDiscipline(LocalDateTime.now().plusDays(5)) // Date future
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .comporters(new HashSet<>()) // Utilisez 'comporters' pour correspondre à votre entité
                .offres(new HashSet<>())
                .build();
        disciplineNatationSynchro = entityManager.persistAndFlush(disciplineNatationSynchro);

        disciplineJudoKata = Discipline.builder()
                .nomDiscipline("Judo Kata")
                .dateDiscipline(LocalDateTime.now().plusDays(15)) // Date future
                .nbPlaceDispo(50)
                .adresse(adresseLyon)
                .comporters(new HashSet<>())
                .offres(new HashSet<>())
                .build();
        disciplineJudoKata = entityManager.persistAndFlush(disciplineJudoKata);
        disciplineEscrimeFleuret = Discipline.builder()
                .nomDiscipline("Escrime Fleuret")
                .dateDiscipline(LocalDateTime.now().plusSeconds(10)) // PAS LocalDateTime.now().minusDays(1)
                .nbPlaceDispo(20)
                .adresse(adresseParis)
                .comporters(new HashSet<>())
                .offres(new HashSet<>())
                .build();
        entityManager.persistAndFlush(disciplineEscrimeFleuret);

        // Liaison Discipline-Epreuve via Comporter
        Comporter comporterNatation = Comporter.builder()
                .id(new ComporterKey(epreuveNatationCourse.getIdEpreuve(), disciplineNatationSynchro.getIdDiscipline()))
                .epreuve(epreuveNatationCourse).discipline(disciplineNatationSynchro).jrDeMedaille(true).build();
        entityManager.persistAndFlush(comporterNatation);

        Comporter comporterJudo = Comporter.builder()
                .id(new ComporterKey(epreuveJudoCombat.getIdEpreuve(), disciplineJudoKata.getIdDiscipline()))
                .epreuve(epreuveJudoCombat).discipline(disciplineJudoKata).jrDeMedaille(false).build();
        entityManager.persistAndFlush(comporterJudo);

        // Ajouter une autre liaison pour tester la recherche par plusieurs ID d'épreuves
        Comporter comporterNatationSurJudoEpreuve = Comporter.builder()
                .id(new ComporterKey(epreuveJudoCombat.getIdEpreuve(), disciplineNatationSynchro.getIdDiscipline()))
                .epreuve(epreuveJudoCombat).discipline(disciplineNatationSynchro).jrDeMedaille(false).build();
        entityManager.persistAndFlush(comporterNatationSurJudoEpreuve);
    }

    @Test
    void findFutureDisciplinesWithAdresse_shouldReturnPagedFutureDisciplines() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        // Utilise une date "now" qui est FUTURE par rapport à la date de 'disciplineEscrimeFleuret'
        // pour que seules les disciplines "vraiment" futures soient renvoyées.
        LocalDateTime now = LocalDateTime.now().plusSeconds(30); // S'assurer que c'est après disciplineEscrimeFleuret

        // Act
        Page<Discipline> resultPage = disciplineRepository.findFutureDisciplinesWithAdresse(now, pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        // Seules Natation Synchronisée et Judo Kata sont futures par rapport à `now` (+30s).
        // Escrime Fleuret (+10s) sera passée.
        assertThat(resultPage.getContent())
                .hasSize(2)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Judo Kata");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void decrementerPlaces_shouldDecreasePlaces_whenSufficientPlaces() {
        // Arrange
        int placesToDecrement = 10;
        Long disciplineId = disciplineNatationSynchro.getIdDiscipline();
        int initialPlaces = disciplineNatationSynchro.getNbPlaceDispo(); // 100

        // Act
        int updatedRows = disciplineRepository.decrementerPlaces(disciplineId, placesToDecrement);
        entityManager.refresh(disciplineNatationSynchro); // Rafraîchir l'entité pour voir les changements

        // Assert
        assertThat(updatedRows).isEqualTo(1); // Une ligne doit être mise à jour
        assertThat(disciplineNatationSynchro.getNbPlaceDispo()).isEqualTo(initialPlaces - placesToDecrement);
    }

    @Test
    void decrementerPlaces_shouldNotDecreasePlaces_whenInsufficientPlaces() {
        // Arrange
        int placesToDecrement = disciplineJudoKata.getNbPlaceDispo() + 1; // Tenter de décrémenter plus que disponible
        Long disciplineId = disciplineJudoKata.getIdDiscipline();
        int initialPlaces = disciplineJudoKata.getNbPlaceDispo(); // 50

        // Act
        int updatedRows = disciplineRepository.decrementerPlaces(disciplineId, placesToDecrement);
        entityManager.refresh(disciplineJudoKata);

        // Assert
        assertThat(updatedRows).isZero(); // Aucune ligne ne devrait être mise à jour
        assertThat(disciplineJudoKata.getNbPlaceDispo()).isEqualTo(initialPlaces); // Le nombre de places ne doit pas changer
    }

    @Test
    void findDisciplinesByVilleWithAdresse_shouldReturnPagedDisciplinesForGivenVille() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String ville = "Paris"; // adresseParis

        // Act
        Page<Discipline> resultPage = disciplineRepository.findDisciplinesByVilleWithAdresse(ville, pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent())
                .hasSize(2) // Natation Synchronisée et Escrime Fleuret sont à Paris
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Escrime Fleuret");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdWithAdresse_shouldReturnPagedDisciplinesForGivenEpreuveId() {
        // Arrange
        Long epreuveId = epreuveNatationCourse.getIdEpreuve(); // Lié uniquement à Natation Synchronisée
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Discipline> resultPage = disciplineRepository.findDisciplinesByEpreuveIdWithAdresse(epreuveId, pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().getFirst().getNomDiscipline()).isEqualTo("Natation Synchronisée");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdsWithAdresse_shouldReturnSetOfDisciplinesForGivenEpreuveIds() {
        // Arrange
        // epreuveNatationCourse est liée à disciplineNatationSynchro
        // epreuveJudoCombat est liée à disciplineJudoKata ET disciplineNatationSynchro
        List<Long> epreuveIds = List.of(epreuveNatationCourse.getIdEpreuve(), epreuveJudoCombat.getIdEpreuve());

        // Act
        Set<Discipline> resultSet = disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(epreuveIds);

        // Assert
        assertThat(resultSet).isNotNull();
        assertThat(resultSet)
                .hasSize(2) // Natation Synchronisée (via les deux épreuves) et Judo Kata (via une épreuve)
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Judo Kata");
        Optional<Discipline> natationOpt = resultSet.stream().filter(d -> d.getNomDiscipline().equals("Natation Synchronisée")).findFirst();
        assertThat(natationOpt).isPresent();
        assertThat(natationOpt.get().getAdresse()).isNotNull();
    }

    @Test
    void findDisciplinesByEpreuveIdsWithAdresse_shouldReturnEmptySet_whenNoMatchingEpreuveIds() {
        // Arrange
        List<Long> epreuveIdsNonExistants = List.of(999L, 888L); // IDs qui n'existent pas

        // Act
        Set<Discipline> resultSet = disciplineRepository.findDisciplinesByEpreuveIdsWithAdresse(epreuveIdsNonExistants);

        // Assert
        assertThat(resultSet).isNotNull();
        assertThat(resultSet).isEmpty();
    }


    @Test
    void findAllWithAdresse_shouldReturnAllDisciplinesWithAdresse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Discipline> resultPage = disciplineRepository.findAllWithAdresse(pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        // Il y a 3 disciplines au total créées dans setUp
        assertThat(resultPage.getContent()).hasSize(3);
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
    }
}*/