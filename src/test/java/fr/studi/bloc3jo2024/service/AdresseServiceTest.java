package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.PaysRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AdresseService}.
 */
@ExtendWith(MockitoExtension.class)
class AdresseServiceTest {

    @Mock
    private AdresseRepository adresseRepository;

    @Mock
    private PaysRepository paysRepository;

    @InjectMocks
    private AdresseService adresseService;

    private Adresse adresseValide; // Renommé pour clarté
    private Pays paysFrance;
    private Discipline disciplineAssociee;
    private final Long adresseIdExistant = 1L;
    private final UUID utilisateurIdExistant = UUID.randomUUID();
    private final Long paysIdExistant = 100L;

    private static final String ADRESSE_NON_TROUVEE_MSG_PREFIX = "Adresse non trouvée avec l'ID : ";
    private static final String PAYS_NON_TROUVE_MSG_PREFIX = "Pays non trouvé avec l'ID : ";


    @BeforeEach
    void setUp() {
        paysFrance = Pays.builder().idPays(paysIdExistant).nomPays("France").build();
        adresseValide = Adresse.builder()
                .idAdresse(adresseIdExistant)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(paysFrance)
                .build();

        final Long disciplineId = 10L;
        disciplineAssociee = Discipline.builder().idDiscipline(disciplineId).nomDiscipline("Natation").build();
        // Simuler qu'une discipline est associée à adresseValide pour certains tests
        // disciplineAssociee.setAdresse(adresseValide); // Ceci serait fait par le setup de données si nécessaire
    }

    @Test
    void creerAdresseSiNonExistante_shouldReturnExistingAdresse_whenAdresseFoundByDetailsAndPaysExists() {
        // Arrange
        // Le pays de l'adresseValide doit exister dans la "base de données" (mock PaysRepository)
        when(paysRepository.findById(adresseValide.getPays().getIdPays())).thenReturn(Optional.of(paysFrance));
        // L'adresse avec ces détails existe déjà
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), paysFrance
        )).thenReturn(Optional.of(adresseValide));

        // Act
        Adresse result = adresseService.creerAdresseSiNonExistante(adresseValide);

        // Assert
        assertNotNull(result);
        assertEquals(adresseValide.getIdAdresse(), result.getIdAdresse());
        verify(paysRepository).findById(adresseValide.getPays().getIdPays());
        verify(adresseRepository).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), paysFrance
        );
        verify(adresseRepository, never()).save(any(Adresse.class)); // Ne doit pas sauvegarder une nouvelle adresse
    }

    @Test
    void creerAdresseSiNonExistante_shouldThrowIllegalArgumentException_whenPaysInAdresseIsNull() {
        // Arrange
        Adresse adresseSansPays = Adresse.builder().numeroRue(1).nomRue("Test").ville("Test").codePostal("12345").pays(null).build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> adresseService.creerAdresseSiNonExistante(adresseSansPays));
        assertEquals("Les informations du pays (avec ID) sont requises pour créer ou vérifier une adresse.", thrown.getMessage());
        verify(paysRepository, never()).findById(any());
        verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(any(),any(),any(),any(),any());
        verify(adresseRepository, never()).save(any());
    }

    @Test
    void creerAdresseSiNonExistante_shouldThrowIllegalArgumentException_whenPaysIdInAdresseIsNull() {
        // Arrange
        Pays paysSansId = Pays.builder().nomPays("France").build(); // ID du pays est null
        Adresse adresseAvecPaysSansId = Adresse.builder().numeroRue(1).nomRue("Test").ville("Test").codePostal("12345").pays(paysSansId).build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> adresseService.creerAdresseSiNonExistante(adresseAvecPaysSansId));
        assertEquals("Les informations du pays (avec ID) sont requises pour créer ou vérifier une adresse.", thrown.getMessage());
    }

    @Test
    void creerAdresseSiNonExistante_shouldThrowResourceNotFoundException_whenReferencedPaysNotInDb() {
        // Arrange
        Pays paysNonEnDb = Pays.builder().idPays(999L).nomPays("Pays Inconnu").build();
        Adresse adresseAvecPaysNonExistant = Adresse.builder()
                .numeroRue(1).nomRue("Test").ville("Test").codePostal("12345").pays(paysNonEnDb).build();

        when(paysRepository.findById(999L)).thenReturn(Optional.empty()); // Le pays n'est pas trouvé

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.creerAdresseSiNonExistante(adresseAvecPaysNonExistant));
        assertEquals(PAYS_NON_TROUVE_MSG_PREFIX + "999", thrown.getMessage());
    }


    @Test
    void creerAdresseSiNonExistante_shouldSaveAndReturnNewAdresse_whenAdresseNotFoundAndPaysExists() {
        // Arrange
        Adresse nouvelleAdresseInput = Adresse.builder() // Adresse telle qu'elle vient du DTO, sans ID
                .numeroRue(20).nomRue("Avenue des Champs-Élysées").ville("Paris").codePostal("75008").pays(paysFrance)
                .build();
        // Adresse telle qu'elle serait retournée par le repository après sauvegarde (avec un ID)
        Adresse adresseSauvegardeeSimulee = Adresse.builder()
                .idAdresse(2L).numeroRue(20).nomRue("Avenue des Champs-Élysées").ville("Paris").codePostal("75008").pays(paysFrance)
                .build();

        when(paysRepository.findById(paysFrance.getIdPays())).thenReturn(Optional.of(paysFrance)); // Le pays existe
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                nouvelleAdresseInput.getNumeroRue(), nouvelleAdresseInput.getNomRue(), nouvelleAdresseInput.getVille(), nouvelleAdresseInput.getCodePostal(), paysFrance
        )).thenReturn(Optional.empty()); // L'adresse n'existe pas encore avec ces détails
        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresseSauvegardeeSimulee);

        // Act
        Adresse result = adresseService.creerAdresseSiNonExistante(nouvelleAdresseInput);

        // Assert
        assertNotNull(result);
        assertEquals(adresseSauvegardeeSimulee.getIdAdresse(), result.getIdAdresse()); // ID de l'adresse sauvegardée
        assertEquals(nouvelleAdresseInput.getVille(), result.getVille());

        // Vérifier que save a été appelé avec une entité Adresse dont le pays est l'instance persistée 'paysFrance'
        ArgumentCaptor<Adresse> adresseCaptor = ArgumentCaptor.forClass(Adresse.class);
        verify(adresseRepository).save(adresseCaptor.capture());
        assertEquals(paysFrance, adresseCaptor.getValue().getPays(), "L'entité Pays persistée doit être utilisée.");
        assertNull(adresseCaptor.getValue().getIdAdresse(), "L'ID doit être null avant la sauvegarde pour une nouvelle adresse.");
    }


    @Test
    void getAdresseById_shouldReturnAdresseWhenFound() {
        when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
        Adresse result = adresseService.getAdresseById(adresseIdExistant);
        assertNotNull(result);
        assertEquals(adresseIdExistant, result.getIdAdresse());
        verify(adresseRepository).findById(adresseIdExistant);
    }

    @Test
    void getAdresseById_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        Long idInexistant = 99L;
        when(adresseRepository.findById(idInexistant)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.getAdresseById(idInexistant));
        assertEquals(ADRESSE_NON_TROUVEE_MSG_PREFIX + idInexistant, thrown.getMessage());
        verify(adresseRepository).findById(idInexistant);
    }

    // ... autres tests pour getAllAdresses, getAdressesByUtilisateurId etc. sont probablement corrects dans leur structure.

    @Test
    void adresseExisteDeja_shouldReturnFalse_whenPaysInAdresseIsNull() {
        // Arrange
        Adresse adresseSansPays = Adresse.builder().numeroRue(1).nomRue("Test").ville("V").codePostal("1").pays(null).build();

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresseSansPays);

        // Assert
        assertFalse(exists, "L'adresse ne devrait pas exister si les informations du pays sont manquantes.");
        // CORRECTION: Vérifier que la méthode du repository n'est JAMAIS appelée si le pays est null.
        verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(anyInt(), anyString(), anyString(), anyString(), any(Pays.class));
        verify(paysRepository, never()).findById(any()); // Ne devrait même pas essayer de charger le pays
    }

    @Test
    void adresseExisteDeja_shouldReturnFalse_whenPaysIdInAdresseIsNull() {
        // Arrange
        Pays paysSansId = Pays.builder().nomPays("France").build(); // ID est null
        Adresse adresseAvecPaysSansId = Adresse.builder().numeroRue(1).nomRue("Test").ville("V").codePostal("1").pays(paysSansId).build();

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresseAvecPaysSansId);

        // Assert
        assertFalse(exists, "L'adresse ne devrait pas exister si l'ID du pays est null.");
        verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(anyInt(), anyString(), anyString(), anyString(), any(Pays.class));
        verify(paysRepository, never()).findById(any());
    }

    @Test
    void adresseExisteDeja_shouldReturnFalse_whenReferencedPaysNotInDb() {
        // Arrange
        Pays paysNonEnDb = Pays.builder().idPays(999L).nomPays("Inconnu").build();
        Adresse adresseAvecPaysNonEnDb = Adresse.builder()
                .numeroRue(1).nomRue("Test").ville("V").codePostal("1").pays(paysNonEnDb).build();
        when(paysRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresseAvecPaysNonEnDb);

        // Assert
        assertFalse(exists, "L'adresse ne devrait pas exister si son pays n'est pas en base.");
        verify(paysRepository).findById(999L);
        // findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays ne sera pas appelé car le pays n'est pas trouvé
        verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(anyInt(), anyString(), anyString(), anyString(), any(Pays.class));
    }

    @Test
    void adresseExisteDeja_shouldCallRepositoryAndReturnTrue_whenPaysExistsAndAdresseDetailsMatch() {
        // Arrange
        when(paysRepository.findById(adresseValide.getPays().getIdPays())).thenReturn(Optional.of(paysFrance));
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), paysFrance
        )).thenReturn(Optional.of(adresseValide));

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresseValide);

        // Assert
        assertTrue(exists);
        verify(paysRepository).findById(adresseValide.getPays().getIdPays());
        verify(adresseRepository).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresseValide.getNumeroRue(), adresseValide.getNomRue(), adresseValide.getVille(), adresseValide.getCodePostal(), paysFrance);
    }


    @Test
    void getIdAdresseSiExistante_shouldReturnNull_whenPaysInAdresseIsNull() {
        // Arrange
        Adresse adresseSansPays = Adresse.builder().numeroRue(1).nomRue("Test").ville("V").codePostal("1").pays(null).build();

        // Act
        Long resultId = adresseService.getIdAdresseSiExistante(adresseSansPays);

        // Assert
        assertNull(resultId);
        // Vérifier que le repo n'est pas appelé si le pays est null, car le service doit court-circuiter.
        verify(adresseRepository, never()).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(any(), any(), any(), any(), any());
        verify(paysRepository, never()).findById(any());
    }

    // ... (Les autres tests comme isAdresseLieeAUnDiscipline, etc. devraient être revus avec la même rigueur)

    @Test
    void deleteAdresse_shouldDeleteAdresse_whenNotLinkedToDisciplineAndExists() {
        // Arrange
        when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseIdExistant)).thenReturn(false); // Non liée
        doNothing().when(adresseRepository).delete(adresseValide);

        // Act & Assert
        assertDoesNotThrow(() -> adresseService.deleteAdresse(adresseIdExistant));

        verify(adresseRepository).findById(adresseIdExistant);
        verify(adresseRepository).isAdresseLieeAUnDiscipline(adresseIdExistant);
        verify(adresseRepository).delete(adresseValide);
    }

    @Test
    void deleteAdresse_shouldThrowResourceNotFoundException_whenAdresseNotFound() {
        // Arrange
        Long idInexistant = 99L;
        when(adresseRepository.findById(idInexistant)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.deleteAdresse(idInexistant));
        assertEquals(ADRESSE_NON_TROUVEE_MSG_PREFIX + idInexistant, thrown.getMessage());

        verify(adresseRepository).findById(idInexistant);
        verify(adresseRepository, never()).isAdresseLieeAUnDiscipline(anyLong());
        verify(adresseRepository, never()).delete(any(Adresse.class));
    }

    @Test
    void deleteAdresse_shouldThrowAdresseLieeAUneDisciplineException_whenLinked() {
        // Arrange
        when(adresseRepository.findById(adresseIdExistant)).thenReturn(Optional.of(adresseValide));
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseIdExistant)).thenReturn(true); // Liée !

        // Act & Assert
        AdresseLieeAUneDisciplineException thrown = assertThrows(AdresseLieeAUneDisciplineException.class,
                () -> adresseService.deleteAdresse(adresseIdExistant));
        assertTrue(thrown.getMessage().contains("L'adresse avec l'ID " + adresseIdExistant + " est liée"));

        verify(adresseRepository).findById(adresseIdExistant);
        verify(adresseRepository).isAdresseLieeAUnDiscipline(adresseIdExistant);
        verify(adresseRepository, never()).delete(any(Adresse.class));
    }
}