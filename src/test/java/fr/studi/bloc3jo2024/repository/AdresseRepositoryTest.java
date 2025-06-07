package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.integration.AbstractPostgresIntegrationTest;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdresseRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Pays francePays;
    private Adresse adressePersisted1;
    private Adresse adressePersisted2;
    private Role roleUser;

    /**
     * Prépare les données nécessaires avant l'exécution de chaque méthode de test.
     * Grâce au rollback transactionnel de @DataJpaTest, chaque test s'exécute
     * sur un jeu de données "propre" basé sur ce setup.
     */
    @BeforeEach
    void setUp() {
        // Initialisation du rôle USER
        roleUser = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :nom", Role.class)
                .setParameter("nom", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setTypeRole(TypeRole.USER);
                    return entityManager.persistAndFlush(newRole);
                });

        // Initialisation du pays "France"
        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = Pays.builder().nomPays("France").build();
                    return entityManager.persistAndFlush(newPays);
                });

        // Création et persistence des adresses de test
        adressePersisted1 = Adresse.builder()
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(francePays)
                .utilisateurs(new HashSet<>())
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted1);

        adressePersisted2 = Adresse.builder()
                .numeroRue(25)
                .nomRue("Boulevard Haussmann")
                .ville("Paris")
                .codePostal("75009")
                .pays(francePays)
                .utilisateurs(new HashSet<>())
                .disciplines(new HashSet<>())
                .build();
        entityManager.persistAndFlush(adressePersisted2);
    }

    @Test
    @DisplayName("findByVille doit retourner les adresses correspondantes")
    void findByVille_shouldReturnMatchingAdresses() {
        List<Adresse> adressesParis = adresseRepository.findByVille("Paris");
        assertNotNull(adressesParis, "La liste ne doit pas être null.");
        assertEquals(2, adressesParis.size(), "Devrait trouver 2 adresses à Paris.");
    }

    @Test
    @DisplayName("findByVille doit retourner une liste vide si aucune adresse ne correspond")
    void findByVille_shouldReturnEmptyListWhenNoMatch() {
        List<Adresse> adressesInconnues = adresseRepository.findByVille("VilleInconnue");
        assertNotNull(adressesInconnues, "La liste ne doit pas être null.");
        assertTrue(adressesInconnues.isEmpty(), "La liste doit être vide pour une ville inconnue.");
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner l'adresse si elle existe")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnAdresseWhenExists() {
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                10, "Rue de la Paix", "Paris", "75001", francePays
        );
        assertTrue(found.isPresent(), "L'adresse (adressePersisted1) doit être trouvée.");
        assertEquals(adressePersisted1.getIdAdresse(), found.get().getIdAdresse(), "L'ID de l'adresse trouvée doit correspondre.");
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner Optional vide si l'adresse n'existe pas")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnEmptyWhenNotExists() {
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                999, "Rue Inconnue", "VilleInconnue", "00000", francePays
        );
        assertFalse(found.isPresent(), "Aucune adresse ne doit être trouvée.");
    }

    @Test
    @DisplayName("findByUtilisateurs_IdUtilisateur doit retourner les adresses pour un utilisateur")
    void findByUtilisateurs_IdUtilisateur_shouldReturnAdressesForUser() {
        Utilisateur utilisateur = Utilisateur.builder()
                .email("user-test-" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("UserNom")
                .prenom("UserPrenom")
                .dateNaissance(LocalDate.now().minusYears(25))
                .adresse(adressePersisted1) // L'utilisateur habite à adressePersisted1
                .role(roleUser)
                .telephones(new ArrayList<>())
                .authTokensTemporaires(new ArrayList<>())
                .paniers(new ArrayList<>())
                .billets(new ArrayList<>())
                .build();
        Utilisateur persistedUtilisateur = entityManager.persistAndFlush(utilisateur);

        List<Adresse> adressesTrouvees = adresseRepository.findByUtilisateurs_IdUtilisateur(persistedUtilisateur.getIdUtilisateur());

        assertNotNull(adressesTrouvees, "La liste ne doit pas être null.");
        assertFalse(adressesTrouvees.isEmpty(), "Devrait trouver au moins une adresse pour cet utilisateur.");
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse(), "L'adresse trouvée doit être adressePersisted1.");
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner true si l'adresse est liée")
    void isAdresseLieeAUnDiscipline_shouldReturnTrueWhenLinked() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Escrime Test")
                .dateDiscipline(LocalDateTime.now().plusDays(30))
                .nbPlaceDispo(50)
                .adresse(adressePersisted1) // La discipline a lieu à adressePersisted1
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted1.getIdAdresse());
        assertTrue(isLinked, "adressePersisted1 doit être marquée comme liée.");
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner false si l'adresse n'est pas liée")
    void isAdresseLieeAUnDiscipline_shouldReturnFalseWhenNotLinked() {
        // adressePersisted2 n'a pas de discipline liée dans le setup ou ce test.
        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted2.getIdAdresse());
        assertFalse(isLinked, "adressePersisted2 ne doit pas être marquée comme liée.");
    }

    @Test
    @DisplayName("findByDisciplinesAndPays_IdPays doit retourner les adresses correspondantes")
    void findByDisciplinesAndPays_IdPays_shouldReturnMatchingAdresses() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Judo Test")
                .dateDiscipline(LocalDateTime.now().plusMonths(2))
                .nbPlaceDispo(30)
                .adresse(adressePersisted1) // Discipline à adressePersisted1 (en France)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        List<Adresse> result = adresseRepository.findByDisciplinesAndPays_IdPays(discipline, francePays.getIdPays());

        assertNotNull(result, "La liste ne doit pas être null.");
        assertFalse(result.isEmpty(), "La liste ne doit pas être vide.");
        assertEquals(adressePersisted1.getIdAdresse(), result.getFirst().getIdAdresse(), "L'adresse trouvée doit être adressePersisted1.");
    }

    @Test
    @DisplayName("findByDisciplines doit retourner l'adresse associée à la discipline")
    void findByDisciplines_shouldReturnAdresseForDiscipline() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Tir à l'arc Test")
                .dateDiscipline(LocalDateTime.now().plusDays(60))
                .nbPlaceDispo(20)
                .adresse(adressePersisted2) // Discipline à adressePersisted2
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        Optional<Adresse> foundAdresseOpt = adresseRepository.findByDisciplines(discipline);

        assertTrue(foundAdresseOpt.isPresent(), "Une adresse doit être trouvée pour la discipline.");
        assertEquals(adressePersisted2.getIdAdresse(), foundAdresseOpt.get().getIdAdresse(), "L'ID de l'adresse doit correspondre à adressePersisted2.");
    }

    @Test
    @DisplayName("findByDisciplinesContaining doit retourner une liste d'adresses pour la discipline")
    void findByDisciplinesContaining_shouldReturnListOfAdressesForDiscipline() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Natation Synchronisée Test")
                .dateDiscipline(LocalDateTime.now().plusDays(90))
                .nbPlaceDispo(15)
                .adresse(adressePersisted1) // Discipline à adressePersisted1
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        List<Adresse> adressesTrouvees = adresseRepository.findByDisciplinesContaining(discipline);

        assertNotNull(adressesTrouvees);
        assertFalse(adressesTrouvees.isEmpty(), "La liste ne doit pas être vide.");
        assertEquals(1, adressesTrouvees.size(), "Devrait trouver une seule adresse pour cette discipline.");
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse());
    }
}
