package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Pays;
import fr.studi.bloc3jo2024.exception.AdresseLieeAUneDisciplineException;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdresseServiceTest {

    @Mock
    private AdresseRepository adresseRepository;

    @InjectMocks
    private AdresseService adresseService;

    private Adresse adresse;
    private Pays pays;
    private Discipline discipline;
    // Champ 'utilisateur' inutile supprimé
    private final Long adresseId = 1L;
    private final UUID userId = UUID.randomUUID();
    private final Long disciplineId = 10L;
    private final Long paysId = 100L;


    @BeforeEach
    void setUp() {
        // Arrange
        pays = Pays.builder().idPays(paysId).nomPays("France").build();
        adresse = Adresse.builder()
                .idAdresse(adresseId)
                .numeroRue(10)
                .nomRue("Rue de la Paix")
                .ville("Paris")
                .codePostal("75001")
                .pays(pays)
                .build();
        discipline = Discipline.builder().idDiscipline(disciplineId).nomDiscipline("Natation").build();
    }

    @Test
    void creerAdresseSiNonExistante_shouldReturnExistingAdresseWhenFound() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.of(adresse));

        // Act
        Adresse result = adresseService.creerAdresseSiNonExistante(adresse);

        // Assert
        assertNotNull(result);
        assertEquals(adresse.getIdAdresse(), result.getIdAdresse()); // Devrait retourner l'adresse existante
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
        verify(adresseRepository, never()).save(any(Adresse.class)); // Save ne devrait pas être appelé
    }

    @Test
    void creerAdresseSiNonExistante_shouldSaveAndReturnNewAdresseWhenNotFound() {
        // Arrange
        Adresse nouvelleAdresse = Adresse.builder()
                .numeroRue(20)
                .nomRue("Avenue des Champs-Élysées")
                .ville("Paris")
                .codePostal("75008")
                .pays(pays)
                .build();
        Adresse savedAdresse = Adresse.builder()
                .idAdresse(2L) // Simule un ID généré par la BDD
                .numeroRue(20)
                .nomRue("Avenue des Champs-Élysées")
                .ville("Paris")
                .codePostal("75008")
                .pays(pays)
                .build();

        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                nouvelleAdresse.getNumeroRue(), nouvelleAdresse.getNomRue(), nouvelleAdresse.getVille(), nouvelleAdresse.getCodePostal(), nouvelleAdresse.getPays()
        )).thenReturn(Optional.empty());
        when(adresseRepository.save(any(Adresse.class))).thenReturn(savedAdresse);

        // Act
        Adresse result = adresseService.creerAdresseSiNonExistante(nouvelleAdresse);

        // Assert
        assertNotNull(result);
        assertEquals(savedAdresse.getIdAdresse(), result.getIdAdresse()); // Devrait retourner l'adresse sauvegardée
        // Supprimé le eq() redondant
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                nouvelleAdresse.getNumeroRue(), nouvelleAdresse.getNomRue(), nouvelleAdresse.getVille(), nouvelleAdresse.getCodePostal(), nouvelleAdresse.getPays()
        );
        verify(adresseRepository, times(1)).save(eq(nouvelleAdresse));
    }

    @Test
    void getAdresseById_shouldReturnAdresseWhenFound() {
        // Arrange
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(adresse));

        // Act
        Adresse result = adresseService.getAdresseById(adresseId);

        // Assert
        assertNotNull(result);
        assertEquals(adresseId, result.getIdAdresse());
        // Supprimé le eq() redondant
        verify(adresseRepository, times(1)).findById(adresseId);
    }

    @Test
    void getAdresseById_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        // Arrange
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.getAdresseById(adresseId));

        assertTrue(thrown.getMessage().contains("Adresse non trouvée avec l'ID : " + adresseId));
        verify(adresseRepository, times(1)).findById(adresseId);
    }

    @Test
    void getAllAdresses_shouldReturnListOfAdresses() {
        // Arrange
        List<Adresse> adresses = Arrays.asList(adresse, Adresse.builder().idAdresse(2L).build());
        when(adresseRepository.findAll()).thenReturn(adresses);

        // Act
        List<Adresse> result = adresseService.getAllAdresses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adresse));
        verify(adresseRepository, times(1)).findAll();
    }

    @Test
    void getAdressesByUtilisateurId_shouldReturnListOfAdresses() {
        // Arrange
        List<Adresse> userAdresses = Arrays.asList(adresse, Adresse.builder().idAdresse(2L).build());
        // Utilise le champ userId
        when(adresseRepository.findByUtilisateurs_IdUtilisateur(userId)).thenReturn(userAdresses);

        // Act
        // Utilise le champ userId
        List<Adresse> result = adresseService.getAdressesByUtilisateurId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adresse));
        // Utilise le champ userId
        verify(adresseRepository, times(1)).findByUtilisateurs_IdUtilisateur(userId);
    }

    @Test
    void getAdresseByDiscipline_shouldReturnAdresseWhenFound() {
        // Arrange
        when(adresseRepository.findByDisciplines(discipline)).thenReturn(Optional.of(adresse));

        // Act
        Adresse result = adresseService.getAdresseByDiscipline(discipline);

        // Assert
        assertNotNull(result);
        assertEquals(adresse.getIdAdresse(), result.getIdAdresse());
        verify(adresseRepository, times(1)).findByDisciplines(discipline);
    }

    @Test
    void getAdresseByDiscipline_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        // Arrange
        when(adresseRepository.findByDisciplines(discipline)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.getAdresseByDiscipline(discipline));

        assertTrue(thrown.getMessage().contains("Adresse non trouvée pour la discipline avec l'ID : " + discipline.getIdDiscipline()));
        verify(adresseRepository, times(1)).findByDisciplines(discipline);
    }

    @Test
    void getAdressesByDisciplineList_shouldReturnListOfAdresses() {
        // Arrange
        List<Adresse> disciplineAdresses = Arrays.asList(adresse, Adresse.builder().idAdresse(2L).build());
        when(adresseRepository.findByDisciplinesContaining(discipline)).thenReturn(disciplineAdresses);

        // Act
        List<Adresse> result = adresseService.getAdressesByDiscipline(discipline);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adresse));
        verify(adresseRepository, times(1)).findByDisciplinesContaining(discipline);
    }


    @Test
    void adresseExisteDeja_shouldReturnTrueWhenExists() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.of(adresse));

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresse);

        // Assert
        assertTrue(exists);
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void adresseExisteDeja_shouldReturnFalseWhenNotExists() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.empty());

        // Act
        boolean exists = adresseService.adresseExisteDeja(adresse);

        // Assert
        assertFalse(exists);
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void getIdAdresseSiExistante_shouldReturnIdWhenExists() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.of(adresse));

        // Act
        Long resultId = adresseService.getIdAdresseSiExistante(adresse);

        // Assert
        assertNotNull(resultId);
        assertEquals(adresseId, resultId);
        // Supprimé le eq() redondant
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void getIdAdresseSiExistante_shouldReturnNullWhenNotExists() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.empty());

        // Act
        Long resultId = adresseService.getIdAdresseSiExistante(adresse);

        // Assert
        assertNull(resultId);
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void isAdresseLieeAUnDiscipline_shouldReturnTrueWhenLinked() {
        // Arrange
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseId)).thenReturn(true);

        // Act
        boolean isLinked = adresseService.isAdresseLieeAUnDiscipline(adresseId);

        // Assert
        assertTrue(isLinked);
        verify(adresseRepository, times(1)).isAdresseLieeAUnDiscipline(adresseId);
    }

    @Test
    void isAdresseLieeAUnDiscipline_shouldReturnFalseWhenNotLinked() {
        // Arrange
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseId)).thenReturn(false);

        // Act
        boolean isLinked = adresseService.isAdresseLieeAUnDiscipline(adresseId);

        // Assert
        assertFalse(isLinked);
        verify(adresseRepository, times(1)).isAdresseLieeAUnDiscipline(adresseId);
    }

    @Test
    void rechercherAdresseComplete_shouldReturnAdresseWhenFound() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.of(adresse));

        // Act
        Adresse result = adresseService.rechercherAdresseComplete(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );

        // Assert
        assertNotNull(result);
        assertEquals(adresse.getIdAdresse(), result.getIdAdresse());
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void rechercherAdresseComplete_shouldReturnNullWhenNotFound() {
        // Arrange
        when(adresseRepository.findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        )).thenReturn(Optional.empty());

        // Act
        Adresse result = adresseService.rechercherAdresseComplete(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );

        // Assert
        assertNull(result);
        verify(adresseRepository, times(1)).findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
                adresse.getNumeroRue(), adresse.getNomRue(), adresse.getVille(), adresse.getCodePostal(), adresse.getPays()
        );
    }

    @Test
    void rechercherAdressesParVillePourDisciplines_shouldReturnListOfAdresses() {
        // Arrange
        List<Adresse> cityAdresses = Arrays.asList(adresse, Adresse.builder().idAdresse(2L).build());
        when(adresseRepository.findByVille(adresse.getVille())).thenReturn(cityAdresses);

        // Act
        List<Adresse> result = adresseService.rechercherAdressesParVillePourDisciplines(adresse.getVille());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adresse));
        // Supprimé le eq() redondant
        verify(adresseRepository, times(1)).findByVille(adresse.getVille());
    }

    @Test
    void rechercherAdressesParDisciplineEtPays_shouldReturnListOfAdresses() {
        // Arrange
        List<Adresse> filteredAdresses = Arrays.asList(adresse, Adresse.builder().idAdresse(2L).build());
        when(adresseRepository.findByDisciplinesAndPays_IdPays(discipline, paysId)).thenReturn(filteredAdresses);

        // Act
        List<Adresse> result = adresseService.rechercherAdressesParDisciplineEtPays(discipline, paysId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(adresse));
        verify(adresseRepository, times(1)).findByDisciplinesAndPays_IdPays(discipline, paysId);
    }

    @Test
    void updateAdresse_shouldUpdateAndReturnAdresseWhenFound() {
        // Arrange
        Adresse nouvelleAdresseDetails = Adresse.builder()
                .numeroRue(99)
                .nomRue("Nouvelle Rue")
                .ville("Nouvelle Ville")
                .codePostal("99999")
                // Utilise nomPays basé sur le code fourni par l'utilisateur
                .pays(Pays.builder().idPays(200L).nomPays("Canada").build())
                .build();

        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(adresse));
        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresse); // Retourne l'instance mise à jour

        // Act
        Adresse updatedAdresse = adresseService.updateAdresse(adresseId, nouvelleAdresseDetails);

        // Assert
        assertNotNull(updatedAdresse);
        assertEquals(adresseId, updatedAdresse.getIdAdresse()); // L'ID devrait rester le même
        assertEquals(nouvelleAdresseDetails.getNumeroRue(), updatedAdresse.getNumeroRue());
        assertEquals(nouvelleAdresseDetails.getNomRue(), updatedAdresse.getNomRue());
        assertEquals(nouvelleAdresseDetails.getVille(), updatedAdresse.getVille());
        assertEquals(nouvelleAdresseDetails.getCodePostal(), updatedAdresse.getCodePostal());
        assertEquals(nouvelleAdresseDetails.getPays(), updatedAdresse.getPays());

        // Supprimé le eq() redondant
        verify(adresseRepository, times(1)).findById(adresseId);
        verify(adresseRepository, times(1)).save(eq(adresse)); // Vérifie que save a été appelé sur l'adresse existante
    }

    @Test
    void updateAdresse_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        // Arrange
        Adresse nouvelleAdresseDetails = Adresse.builder().build();
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.updateAdresse(adresseId, nouvelleAdresseDetails));

        assertTrue(thrown.getMessage().contains("Adresse non trouvée avec l'ID : " + adresseId));
        verify(adresseRepository, times(1)).findById(adresseId);
        verify(adresseRepository, never()).save(any(Adresse.class)); // Save ne devrait pas être appelé
    }

    @Test
    void deleteAdresse_shouldDeleteAdresseWhenNotLinkedToDiscipline() {
        // Arrange
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(adresse));
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseId)).thenReturn(false);
        doNothing().when(adresseRepository).delete(adresse);

        // Act
        adresseService.deleteAdresse(adresseId);

        // Assert
        verify(adresseRepository, times(1)).findById(adresseId);
        verify(adresseRepository, times(1)).isAdresseLieeAUnDiscipline(adresseId);
        verify(adresseRepository, times(1)).delete(adresse);
    }

    @Test
    void deleteAdresse_shouldThrowResourceNotFoundExceptionWhenNotFound() {
        // Arrange
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class,
                () -> adresseService.deleteAdresse(adresseId));

        assertTrue(thrown.getMessage().contains("Adresse non trouvée avec l'ID : " + adresseId));
        verify(adresseRepository, times(1)).findById(adresseId);
        verify(adresseRepository, never()).isAdresseLieeAUnDiscipline(anyLong());
        verify(adresseRepository, never()).delete(any(Adresse.class)); // Ne devrait pas supprimer
    }

    @Test
    void deleteAdresse_shouldThrowAdresseLieeAUneDisciplineExceptionWhenLinkedToDiscipline() {
        // Arrange
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(adresse));
        when(adresseRepository.isAdresseLieeAUnDiscipline(adresseId)).thenReturn(true);

        // Act & Assert
        AdresseLieeAUneDisciplineException thrown = assertThrows(AdresseLieeAUneDisciplineException.class,
                () -> adresseService.deleteAdresse(adresseId));

        assertTrue(thrown.getMessage().contains("L'adresse avec l'ID " + adresseId + " est liée à un ou plusieurs événements et ne peut pas être supprimée."));
        verify(adresseRepository, times(1)).findById(adresseId);
        verify(adresseRepository, times(1)).isAdresseLieeAUnDiscipline(adresseId);
        verify(adresseRepository, never()).delete(any(Adresse.class)); // Ne devrait pas supprimer
    }
}
