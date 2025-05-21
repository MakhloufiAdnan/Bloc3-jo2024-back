package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AdresseRepositoryTest {

    @Autowired
    private AdresseRepository adresseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Pays francePays;
    private Adresse adressePersisted1;
    private Adresse adressePersisted2;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = entityManager.getEntityManager()
                .createQuery("SELECT r FROM Role r WHERE r.typeRole = :nom", Role.class)
                .setParameter("nom", TypeRole.USER)
                .getResultStream().findFirst().orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setTypeRole(TypeRole.USER);
                    return entityManager.persistAndFlush(newRole);
                });

        francePays = entityManager.getEntityManager()
                .createQuery("SELECT p FROM Pays p WHERE p.nomPays = :nom", Pays.class)
                .setParameter("nom", "France")
                .getResultStream().findFirst().orElseGet(() -> {
                    Pays newPays = Pays.builder().nomPays("France").build();
                    return entityManager.persistAndFlush(newPays);
                });

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
        assertNotNull(adressesParis);
        assertEquals(2, adressesParis.size());
    }

    @Test
    @DisplayName("findByVille doit retourner une liste vide si aucune adresse ne correspond")
    void findByVille_shouldReturnEmptyListWhenNoMatch() {
        List<Adresse> adressesInconnues = adresseRepository.findByVille("VilleInconnue");
        assertNotNull(adressesInconnues);
        assertTrue(adressesInconnues.isEmpty());
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner l'adresse si elle existe")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnAdresseWhenExists() {
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                10, "Rue de la Paix", "Paris", "75001", francePays
        );
        assertTrue(found.isPresent());
        assertEquals(adressePersisted1.getIdAdresse(), found.get().getIdAdresse());
    }

    @Test
    @DisplayName("findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays doit retourner Optional vide si l'adresse n'existe pas")
    void findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays_shouldReturnEmptyWhenNotExists() {
        Optional<Adresse> found = adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                999, "Rue Inconnue", "VilleInconnue", "00000", francePays
        );
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByUtilisateurs_IdUtilisateur doit retourner les adresses pour un utilisateur")
    void findByUtilisateurs_IdUtilisateur_shouldReturnAdressesForUser() {
        Utilisateur utilisateur = Utilisateur.builder()
                .email("user-" + UUID.randomUUID().toString().substring(0,8) + "@example.com")
                .nom("UserNom")
                .prenom("UserPrenom")
                .dateNaissance(LocalDate.now().minusYears(25))
                .adresse(adressePersisted1)
                .role(roleUser)
                .telephones(new ArrayList<>())
                .authTokensTemporaires(new ArrayList<>())
                .paniers(new ArrayList<>())
                .billets(new ArrayList<>())
                .build();
        Utilisateur persistedUtilisateur = entityManager.persistAndFlush(utilisateur);

        List<Adresse> adressesTrouvees = adresseRepository.findByUtilisateurs_IdUtilisateur(persistedUtilisateur.getIdUtilisateur());
        assertNotNull(adressesTrouvees);
        assertFalse(adressesTrouvees.isEmpty());
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse());
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner true si l'adresse est liée")
    void isAdresseLieeAUnDiscipline_shouldReturnTrueWhenLinked() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Escrime")
                .dateDiscipline(LocalDateTime.now().plusDays(30))
                .nbPlaceDispo(50)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted1.getIdAdresse());
        assertTrue(isLinked);
    }

    @Test
    @DisplayName("isAdresseLieeAUnDiscipline doit retourner false si l'adresse n'est pas liée")
    void isAdresseLieeAUnDiscipline_shouldReturnFalseWhenNotLinked() {
        boolean isLinked = adresseRepository.isAdresseLieeAUnDiscipline(adressePersisted2.getIdAdresse());
        assertFalse(isLinked);
    }

    @Test
    @DisplayName("findByDisciplinesAndPays_IdPays doit retourner les adresses correspondantes")
    void findByDisciplinesAndPays_IdPays_shouldReturnMatchingAdresses() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Judo")
                .dateDiscipline(LocalDateTime.now().plusMonths(2))
                .nbPlaceDispo(30)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        List<Adresse> result = adresseRepository.findByDisciplinesAndPays_IdPays(discipline, francePays.getIdPays());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(adressePersisted1.getIdAdresse(), result.getFirst().getIdAdresse());
    }

    @Test
    @DisplayName("findByDisciplines doit retourner l'adresse associée à la discipline")
    void findByDisciplines_shouldReturnAdresseForDiscipline() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Tir à l'arc")
                .dateDiscipline(LocalDateTime.now().plusDays(60))
                .nbPlaceDispo(20)
                .adresse(adressePersisted2)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        Optional<Adresse> foundAdresseOpt = adresseRepository.findByDisciplines(discipline);
        assertTrue(foundAdresseOpt.isPresent());
        assertEquals(adressePersisted2.getIdAdresse(), foundAdresseOpt.get().getIdAdresse());
    }

    @Test
    @DisplayName("findByDisciplinesContaining doit retourner une liste d'adresses pour la discipline")
    void findByDisciplinesContaining_shouldReturnListOfAdressesForDiscipline() {
        Discipline discipline = Discipline.builder()
                .nomDiscipline("Natation Synchronisée")
                .dateDiscipline(LocalDateTime.now().plusDays(90))
                .nbPlaceDispo(15)
                .adresse(adressePersisted1)
                .offres(new HashSet<>())
                .comporters(new HashSet<>())
                .build();
        entityManager.persistAndFlush(discipline);

        List<Adresse> adressesTrouvees = adresseRepository.findByDisciplinesContaining(discipline);
        assertNotNull(adressesTrouvees);
        assertFalse(adressesTrouvees.isEmpty());
        assertEquals(1, adressesTrouvees.size());
        assertEquals(adressePersisted1.getIdAdresse(), adressesTrouvees.getFirst().getIdAdresse());
    }
}