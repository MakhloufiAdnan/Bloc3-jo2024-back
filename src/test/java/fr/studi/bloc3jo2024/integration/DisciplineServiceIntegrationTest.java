package fr.studi.bloc3jo2024.integration;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.repository.*;
import fr.studi.bloc3jo2024.service.DisciplineService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class DisciplineServiceIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DisciplineServiceIntegrationTest.class);

    @Autowired
    private DisciplineService disciplineService;

    @Autowired
    private DisciplineRepository disciplineRepository;

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private PaysRepository paysRepository;

    @Autowired
    private EpreuveRepository epreuveRepository;

    @Autowired
    private ComporterRepository comporterRepository;

    @Autowired
    private EntityManager entityManager;


    private Pays testPays;
    private Adresse testAdresse;

    @BeforeEach
    void setUp() {
        comporterRepository.deleteAllInBatch();
        disciplineRepository.deleteAllInBatch();
        epreuveRepository.deleteAllInBatch();
        adresseRepository.deleteAllInBatch();
        paysRepository.deleteAllInBatch();

        testPays = Pays.builder().nomPays("FranceTest").build();
        paysRepository.saveAndFlush(testPays);

        testAdresse = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue de Test")
                .ville("Testville")
                .codePostal("75000")
                .pays(testPays)
                .build();
        adresseRepository.saveAndFlush(testAdresse);

        log.info("Données de test initialisées. Adresse ID: {}", testAdresse.getIdAdresse());
    }

    @Test
    void testCreerDiscipline_succes() {
        CreerDisciplineDto dto = new CreerDisciplineDto(
                "Natation Test",
                LocalDateTime.now().plusDays(10),
                100,
                testAdresse.getIdAdresse()
        );

        Discipline disciplineCreee = disciplineService.creerDiscipline(dto);

        assertNotNull(disciplineCreee);
        assertNotNull(disciplineCreee.getIdDiscipline());
        assertEquals("Natation Test", disciplineCreee.getNomDiscipline());
        assertEquals(100, disciplineCreee.getNbPlaceDispo());
        assertEquals(testAdresse.getIdAdresse(), disciplineCreee.getAdresse().getIdAdresse());

        // Vérifier la persistance
        Discipline disciplineFromDb = disciplineRepository.findById(disciplineCreee.getIdDiscipline()).orElse(null);
        assertNotNull(disciplineFromDb);
        assertEquals("Natation Test", disciplineFromDb.getNomDiscipline());
    }

    @Test
    void testCreerDiscipline_adresseInexistante_throwException() {
        CreerDisciplineDto dto = new CreerDisciplineDto(
                "Escrime Test",
                LocalDateTime.now().plusDays(5),
                50,
                9999L // ID d'adresse inexistant
        );

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            disciplineService.creerDiscipline(dto)
        );
        assertTrue(exception.getMessage().contains("Adresse non trouvée"));
    }

    @Test
    void testCreerDiscipline_datePassee_throwExceptionViaPrePersist() {
        Discipline discipline = new Discipline();
        discipline.setNomDiscipline("Test Date Passée");
        discipline.setDateDiscipline(LocalDateTime.now().minusDays(1)); // Date passée
        discipline.setNbPlaceDispo(10);
        discipline.setAdresse(testAdresse);

        InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> disciplineRepository.saveAndFlush(discipline)
        );

        assertTrue(exception.getMessage().contains("La date de la discipline ne peut pas être dans le passé."));
        assertNotNull(exception.getCause());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("La date de la discipline ne peut pas être dans le passé.", exception.getCause().getMessage());
    }


    @Test
    void testMettreAJourDiscipline_succes() {
        // 1. Créer une discipline initiale
        Discipline disciplineInitiale = Discipline.builder()
                .nomDiscipline("Judo Ancien")
                .dateDiscipline(LocalDateTime.now().plusMonths(1))
                .nbPlaceDispo(200)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(disciplineInitiale);
        Long disciplineId = disciplineInitiale.getIdDiscipline();

        // 2. Préparer le DTO de mise à jour
        MettreAJourDisciplineDto dtoUpdate = new MettreAJourDisciplineDto(
                disciplineId,
                "Judo Nouveau Nom",
                LocalDateTime.now().plusMonths(2),
                150,
                testAdresse.getIdAdresse()
        );

        // 3. Appeler le service
        Discipline disciplineMiseAJour = disciplineService.mettreAJourDiscipline(dtoUpdate);

        // 4. Assertions
        assertNotNull(disciplineMiseAJour);
        assertEquals(disciplineId, disciplineMiseAJour.getIdDiscipline());
        assertEquals("Judo Nouveau Nom", disciplineMiseAJour.getNomDiscipline());
        assertEquals(150, disciplineMiseAJour.getNbPlaceDispo());
        assertEquals(disciplineMiseAJour.getDateDiscipline().getMonth(), LocalDateTime.now().plusMonths(2).getMonth());
    }

    @Test
    void testSupprimerDiscipline_succes() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Tir à l'arc")
                .dateDiscipline(LocalDateTime.now().plusDays(20))
                .nbPlaceDispo(50)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(discipline);
        Long id = discipline.getIdDiscipline();

        assertTrue(disciplineRepository.existsById(id));
        disciplineService.supprimerDiscipline(id);
        assertFalse(disciplineRepository.existsById(id));
    }

    @Test
    void testRetirerPlaces_succes() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Boxe")
                .dateDiscipline(LocalDateTime.now().plusDays(15)) // Assurez-vous que la date est future pour le @PrePersist
                .nbPlaceDispo(100)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(discipline);

        Discipline disciplineMaj = disciplineService.retirerPlaces(discipline.getIdDiscipline(), 10);
        assertEquals(90, disciplineMaj.getNbPlaceDispo());
    }

    @Test
    void testRetirerPlaces_plusQueDisponible_throwException() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Lutte")
                .dateDiscipline(LocalDateTime.now().plusDays(30))
                .nbPlaceDispo(5)
                .adresse(testAdresse)
                .build();
        Discipline savedDiscipline = disciplineRepository.saveAndFlush(discipline);
        entityManager.clear();

        assertThrows(IllegalStateException.class, () ->
            disciplineService.retirerPlaces(savedDiscipline.getIdDiscipline(), 10)
        );
    }


    @Test
    void testAjouterPlaces_succes() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Gymnastique")
                .dateDiscipline(LocalDateTime.now().plusDays(25))
                .nbPlaceDispo(50)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(discipline);

        Discipline disciplineMaj = disciplineService.ajouterPlaces(discipline.getIdDiscipline(), 20);
        assertEquals(70, disciplineMaj.getNbPlaceDispo());
    }

    @Test
    void testGetDisciplinesAvenir() {
        Discipline disciplineFutureProche = Discipline.builder()
                .nomDiscipline("Futur Proche")
                .dateDiscipline(LocalDateTime.now().plusHours(1)) // Date future
                .nbPlaceDispo(10)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(disciplineFutureProche);

        Discipline disciplineFutureLointaine = Discipline.builder()
                .nomDiscipline("Futur Lointain")
                .dateDiscipline(LocalDateTime.now().plusDays(10)) // Date future
                .nbPlaceDispo(10)
                .adresse(testAdresse)
                .build();
        disciplineRepository.saveAndFlush(disciplineFutureLointaine);

        List<Discipline> disciplinesAvenir = disciplineService.getDisciplinesAvenir();

        assertEquals(2, disciplinesAvenir.size());
        // Vérifiez que les disciplines retournées sont bien celles attendues
        assertTrue(disciplinesAvenir.stream().anyMatch(d -> d.getIdDiscipline().equals(disciplineFutureProche.getIdDiscipline())));
        assertTrue(disciplinesAvenir.stream().anyMatch(d -> d.getIdDiscipline().equals(disciplineFutureLointaine.getIdDiscipline())));
        // Vérifiez la condition de date
        assertTrue(disciplinesAvenir.stream().allMatch(d -> d.getDateDiscipline().isAfter(LocalDateTime.now().minusSeconds(1)))); // Marge d'une seconde pour la comparaison
    }

    @Test
    void testFindDisciplinesFiltered_parVille() {
        Pays autrePays = paysRepository.saveAndFlush(Pays.builder().nomPays("AutrePaysTest").build());
        Adresse autreAdresse = adresseRepository.saveAndFlush(Adresse.builder().numeroRue(1).nomRue("Autre Rue").ville("AutreVilleTest").codePostal("00000").pays(autrePays).build());

        disciplineRepository.saveAndFlush(Discipline.builder().nomDiscipline("Disc1 VilleTest").dateDiscipline(LocalDateTime.now().plusDays(1)).nbPlaceDispo(10).adresse(testAdresse).build());
        disciplineRepository.saveAndFlush(Discipline.builder().nomDiscipline("Disc2 AutreVille").dateDiscipline(LocalDateTime.now().plusDays(2)).nbPlaceDispo(10).adresse(autreAdresse).build());

        List<Discipline> resultats = disciplineService.findDisciplinesFiltered("Testville", null, null);
        assertEquals(1, resultats.size());
        assertEquals("Disc1 VilleTest", resultats.getFirst().getNomDiscipline());
    }

    @Test
    void testFindDisciplinesFiltered_parEpreuveId_succes() {
        // 1. Créer des Épreuves
        Epreuve epreuveNatation = epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Natation 100m").isFeatured(false).build());
        Epreuve epreuveCourse = epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Course 100m").isFeatured(false).build());

        // 2. Créer des Disciplines
        Discipline disciplineNat = Discipline.builder().nomDiscipline("Session Natation Matin").dateDiscipline(LocalDateTime.now().plusDays(5)).nbPlaceDispo(50).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplineNat);
        Discipline disciplineCourse = Discipline.builder().nomDiscipline("Session Course Soir").dateDiscipline(LocalDateTime.now().plusDays(5)).nbPlaceDispo(50).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplineCourse);
        Discipline disciplineAutre = Discipline.builder().nomDiscipline("Session FootBall").dateDiscipline(LocalDateTime.now().plusDays(5)).nbPlaceDispo(20).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplineAutre);

        // 3. Lier Disciplines et Épreuves via Comporter
        comporterRepository.saveAndFlush(Comporter.builder().id(new ComporterKey(epreuveNatation.getIdEpreuve(), disciplineNat.getIdDiscipline())).epreuve(epreuveNatation).discipline(disciplineNat).jrDeMedaille(false).build());
        comporterRepository.saveAndFlush(Comporter.builder().id(new ComporterKey(epreuveCourse.getIdEpreuve(), disciplineCourse.getIdDiscipline())).epreuve(epreuveCourse).discipline(disciplineCourse).jrDeMedaille(true).build());

        entityManager.flush(); // S'assurer que les données sont persistées avant la requête
        entityManager.clear(); // Détacher les entités pour forcer un rechargement depuis la BDD

        // 4. Filtrer
        List<Discipline> disciplinesFiltrees = disciplineService.findDisciplinesFiltered(null, null, epreuveNatation.getIdEpreuve());

        // 5. Assertions
        assertNotNull(disciplinesFiltrees);
        assertEquals(1, disciplinesFiltrees.size(), "Devrait trouver une discipline pour l'épreuve de natation");
        assertEquals(disciplineNat.getIdDiscipline(), disciplinesFiltrees.getFirst().getIdDiscipline());
        assertEquals("Session Natation Matin", disciplinesFiltrees.getFirst().getNomDiscipline());
    }


    @Test
    void testGetDisciplinesEnVedette_succes() {
        // 1. Créer des épreuves, certaines en vedette
        Epreuve epreuveVedette1 = epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Finale 100m EpreuveVedette").isFeatured(true).build());
        Epreuve epreuveVedette2 = epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Finale Saut EpreuveVedette").isFeatured(true).build());
        Epreuve epreuveNonVedette = epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Qualification Javelot").isFeatured(false).build());

        // 2. Créer des disciplines
        Discipline disciplinePourVedette1 = Discipline.builder().nomDiscipline("Disc Vedette 1").dateDiscipline(LocalDateTime.now().plusDays(2)).nbPlaceDispo(100).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplinePourVedette1);
        Discipline disciplinePourVedette2 = Discipline.builder().nomDiscipline("Disc Vedette 2").dateDiscipline(LocalDateTime.now().plusDays(3)).nbPlaceDispo(100).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplinePourVedette2);
        Discipline disciplinePourNonVedette = Discipline.builder().nomDiscipline("Disc Non Vedette").dateDiscipline(LocalDateTime.now().plusDays(4)).nbPlaceDispo(100).adresse(testAdresse).build();
        disciplineRepository.saveAndFlush(disciplinePourNonVedette);

        // 3. Lier les disciplines aux épreuves via Comporter
        comporterRepository.saveAndFlush(Comporter.builder().id(new ComporterKey(epreuveVedette1.getIdEpreuve(), disciplinePourVedette1.getIdDiscipline())).epreuve(epreuveVedette1).discipline(disciplinePourVedette1).jrDeMedaille(true).build());
        comporterRepository.saveAndFlush(Comporter.builder().id(new ComporterKey(epreuveVedette2.getIdEpreuve(), disciplinePourVedette2.getIdDiscipline())).epreuve(epreuveVedette2).discipline(disciplinePourVedette2).jrDeMedaille(true).build());
        comporterRepository.saveAndFlush(Comporter.builder().id(new ComporterKey(epreuveNonVedette.getIdEpreuve(), disciplinePourNonVedette.getIdDiscipline())).epreuve(epreuveNonVedette).discipline(disciplinePourNonVedette).jrDeMedaille(false).build());

        entityManager.flush();
        entityManager.clear();

        // 4. Appeler la méthode du service
        Set<Discipline> disciplinesEnVedette = disciplineService.getDisciplinesEnVedette();

        // 5. Assertions
        assertNotNull(disciplinesEnVedette);
        assertEquals(2, disciplinesEnVedette.size(), "Devrait y avoir 2 disciplines en vedette.");
        assertTrue(disciplinesEnVedette.stream().anyMatch(d -> d.getIdDiscipline().equals(disciplinePourVedette1.getIdDiscipline())), "Discipline pour Vedette 1 manquante.");
        assertTrue(disciplinesEnVedette.stream().anyMatch(d -> d.getIdDiscipline().equals(disciplinePourVedette2.getIdDiscipline())), "Discipline pour Vedette 2 manquante.");
    }

    @Test
    void testGetDisciplinesEnVedette_aucuneEpreuveEnVedette() {
        epreuveRepository.saveAndFlush(Epreuve.builder().nomEpreuve("Epreuve Normale 1").isFeatured(false).build());

        Set<Discipline> disciplinesEnVedette = disciplineService.getDisciplinesEnVedette();
        assertTrue(disciplinesEnVedette.isEmpty(), "Ne devrait retourner aucune discipline si aucune épreuve n'est en vedette.");
    }

}