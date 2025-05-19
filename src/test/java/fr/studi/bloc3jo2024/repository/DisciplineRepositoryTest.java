package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.*; // Importer toutes les entités nécessaires
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour {@link DisciplineRepository}.
 * Utilise @DataJpaTest pour configurer un contexte JPA pour les tests,
 * et Testcontainers pour fournir une instance de base de données PostgreSQL.
 */
@Testcontainers
@DataJpaTest
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Est implicite avec Testcontainers et @DataJpaTest si URL est fournie
class DisciplineRepositoryTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers gère le cycle de vie du conteneur
    static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db_discipline_repo_" + UUID.randomUUID().toString().substring(0,8))
            .withUsername("testuser_discipline")
            .withPassword("testpass_discipline");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Recommandé pour les tests
        // registry.add("spring.jpa.show-sql", () -> "true"); // Utile pour le débogage
    }

    @Autowired
    private TestEntityManager entityManager; // Pour préparer les données de test

    @Autowired
    private DisciplineRepository disciplineRepository; // Le repository à tester

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
        // Création des entités de base
        paysFrance = entityManager.persistFlushFind(Pays.builder().nomPays("France").build());

        adresseParis = entityManager.persistFlushFind(Adresse.builder().numeroRue(10).nomRue("Rue de Paris").ville("Paris").codePostal("75001").pays(paysFrance).build());
        adresseLyon = entityManager.persistFlushFind(Adresse.builder().numeroRue(20).nomRue("Rue de Lyon").ville("Lyon").codePostal("69001").pays(paysFrance).build());

        epreuveNatationCourse = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Natation 100m Nage Libre").isFeatured(true).build());
        epreuveJudoCombat = entityManager.persistFlushFind(Epreuve.builder().nomEpreuve("Judo Combat -70kg").isFeatured(false).build());

        // Création des disciplines
        disciplineNatationSynchro = Discipline.builder()
                .nomDiscipline("Natation Synchronisée")
                .dateDiscipline(LocalDateTime.now().plusDays(5))
                .nbPlaceDispo(100)
                .adresse(adresseParis)
                .comporte(new HashSet<>()) // Initialiser les collections
                .offres(new HashSet<>())
                .build();
        disciplineNatationSynchro = entityManager.persistAndFlush(disciplineNatationSynchro);

        disciplineJudoKata = Discipline.builder()
                .nomDiscipline("Judo Kata")
                .dateDiscipline(LocalDateTime.now().plusDays(15))
                .nbPlaceDispo(50)
                .adresse(adresseLyon)
                .comporte(new HashSet<>())
                .offres(new HashSet<>())
                .build();
        disciplineJudoKata = entityManager.persistAndFlush(disciplineJudoKata);

        disciplineEscrimeFleuret = Discipline.builder()
                .nomDiscipline("Escrime Fleuret")
                .dateDiscipline(LocalDateTime.now().minusDays(1)) // Discipline passée
                .nbPlaceDispo(20)
                .adresse(adresseParis) // Même adresse que Natation Synchronisée
                .comporte(new HashSet<>())
                .offres(new HashSet<>())
                .build();
        disciplineEscrimeFleuret = entityManager.persistAndFlush(disciplineEscrimeFleuret);

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
        LocalDateTime now = LocalDateTime.now();

        // Act
        Page<Discipline> resultPage = disciplineRepository.findFutureDisciplinesWithAdresse(now, pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent())
                .hasSize(2) // Natation Synchronisée et Judo Kata sont dans le futur
                .extracting(Discipline::getNomDiscipline)
                .containsExactlyInAnyOrder("Natation Synchronisée", "Judo Kata");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull(); // Vérifie que l'adresse est chargée
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
        Pageable pageable = PageRequest.of(0, 10);
        Long epreuveId = epreuveNatationCourse.getIdEpreuve(); // Lié uniquement à Natation Synchronisée

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
    void findDisciplinesByDateDisciplineWithAdresse_shouldReturnPagedDisciplinesForGivenDate() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime searchDate = disciplineNatationSynchro.getDateDiscipline(); // Date exacte de Natation Synchronisée

        // Act
        Page<Discipline> resultPage = disciplineRepository.findDisciplinesByDateDisciplineWithAdresse(searchDate, pageable);

        // Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().getFirst().getNomDiscipline()).isEqualTo("Natation Synchronisée");
        assertThat(resultPage.getContent().getFirst().getAdresse()).isNotNull();
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
}