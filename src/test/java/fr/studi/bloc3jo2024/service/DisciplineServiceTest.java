package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Comporter;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.repository.AdresseRepository;
import fr.studi.bloc3jo2024.repository.DisciplineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisciplineServiceTest {

    @Mock
    private DisciplineRepository disciplineRepository;

    @Mock
    private AdresseRepository adresseRepository;

    @Mock
    private EpreuveService epreuveService;

    @InjectMocks
    private DisciplineService disciplineService;

    private Adresse dummyAdresse;
    private Discipline dummyDiscipline;
    private CreerDisciplineDto creerDisciplineDto;
    private MettreAJourDisciplineDto mettreAJourDisciplineDto;

    @BeforeEach
    void setUp() {
        dummyAdresse = new Adresse();
        dummyAdresse.setIdAdresse(1L);
        dummyAdresse.setVille("Paris");

        dummyDiscipline = new Discipline();
        dummyDiscipline.setIdDiscipline(1L);
        dummyDiscipline.setNomDiscipline("Natation");
        dummyDiscipline.setDateDiscipline(LocalDateTime.now().plusDays(10));
        dummyDiscipline.setNbPlaceDispo(100);
        dummyDiscipline.setAdresse(dummyAdresse);

        creerDisciplineDto = new CreerDisciplineDto();
        creerDisciplineDto.setNomDiscipline("Athlétisme");
        creerDisciplineDto.setDateDiscipline(LocalDateTime.now().plusDays(20));
        creerDisciplineDto.setNbPlaceDispo(200);
        creerDisciplineDto.setIdAdresse(1L);

        mettreAJourDisciplineDto = new MettreAJourDisciplineDto();
        mettreAJourDisciplineDto.setIdDiscipline(1L);
        mettreAJourDisciplineDto.setNomDiscipline("Natation Synchronisée");
        mettreAJourDisciplineDto.setDateDiscipline(LocalDateTime.now().plusDays(15));
        mettreAJourDisciplineDto.setNbPlaceDispo(150);
        mettreAJourDisciplineDto.setIdAdresse(1L);
    }

    @Test
    void creerDiscipline_Successful() {
        // Arrange
        when(adresseRepository.findById(creerDisciplineDto.getIdAdresse())).thenReturn(Optional.of(dummyAdresse));
        when(disciplineRepository.save(any(Discipline.class))).thenReturn(dummyDiscipline);

        // Act
        Discipline createdDiscipline = disciplineService.creerDiscipline(creerDisciplineDto);

        // Assert
        assertNotNull(createdDiscipline);
        assertEquals("Natation", createdDiscipline.getNomDiscipline()); // Note: Returns dummyDiscipline, not the one built from DTO
        verify(adresseRepository, times(1)).findById(creerDisciplineDto.getIdAdresse());
        verify(disciplineRepository, times(1)).save(any(Discipline.class));
    }

    @Test
    void creerDiscipline_AdresseNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(adresseRepository.findById(creerDisciplineDto.getIdAdresse())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.creerDiscipline(creerDisciplineDto));
        verify(adresseRepository, times(1)).findById(creerDisciplineDto.getIdAdresse());
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void mettreAJourDiscipline_Successful() {
        // Arrange
        when(disciplineRepository.findById(mettreAJourDisciplineDto.getIdDiscipline())).thenReturn(Optional.of(dummyDiscipline));
        when(adresseRepository.findById(mettreAJourDisciplineDto.getIdAdresse())).thenReturn(Optional.of(dummyAdresse));
        when(disciplineRepository.save(any(Discipline.class))).thenReturn(dummyDiscipline);

        // Act
        Discipline updatedDiscipline = disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto);

        // Assert
        assertNotNull(updatedDiscipline);
        assertEquals("Natation Synchronisée", updatedDiscipline.getNomDiscipline()); // Expecting updated name
        assertEquals(150, updatedDiscipline.getNbPlaceDispo()); // Expecting updated places
        verify(disciplineRepository, times(1)).findById(mettreAJourDisciplineDto.getIdDiscipline());
        verify(adresseRepository, times(1)).findById(mettreAJourDisciplineDto.getIdAdresse());
        verify(disciplineRepository, times(1)).save(any(Discipline.class));
    }

    @Test
    void mettreAJourDiscipline_DisciplineNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(disciplineRepository.findById(mettreAJourDisciplineDto.getIdDiscipline())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto));
        verify(disciplineRepository, times(1)).findById(mettreAJourDisciplineDto.getIdDiscipline());
        verify(adresseRepository, times(0)).findById(anyLong());
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void mettreAJourDiscipline_AdresseNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(disciplineRepository.findById(mettreAJourDisciplineDto.getIdDiscipline())).thenReturn(Optional.of(dummyDiscipline));
        when(adresseRepository.findById(mettreAJourDisciplineDto.getIdAdresse())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.mettreAJourDiscipline(mettreAJourDisciplineDto));
        verify(disciplineRepository, times(1)).findById(mettreAJourDisciplineDto.getIdDiscipline());
        verify(adresseRepository, times(1)).findById(mettreAJourDisciplineDto.getIdAdresse());
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void supprimerDiscipline_Successful() {
        // Arrange
        when(disciplineRepository.existsById(1L)).thenReturn(true);
        doNothing().when(disciplineRepository).deleteById(1L);

        // Act
        disciplineService.supprimerDiscipline(1L);

        // Assert
        verify(disciplineRepository, times(1)).existsById(1L);
        verify(disciplineRepository, times(1)).deleteById(1L);
    }

    @Test
    void supprimerDiscipline_NotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(disciplineRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.supprimerDiscipline(1L));
        verify(disciplineRepository, times(1)).existsById(1L);
        verify(disciplineRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void retirerPlaces_Successful() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(dummyDiscipline));
        when(disciplineRepository.decrementerPlaces(1L, 10)).thenReturn(1); // Indicates one row updated
        // We don't need to re-stub findById for the second call inside getDisciplineOrThrow, Mockito handles this.

        // Act
        Discipline updatedDiscipline = disciplineService.retirerPlaces(1L, 10);

        // Assert
        assertNotNull(updatedDiscipline);
        // The actual place count change isn't reflected in dummyDiscipline here,
        // but the repository method was called and the result is the fetched entity.
        // Verified that findById is called twice by getDisciplineOrThrow (before and after decrement)
        verify(disciplineRepository, times(2)).findById(1L);
        verify(disciplineRepository, times(1)).decrementerPlaces(1L, 10);
    }

    @Test
    void retirerPlaces_DisciplineNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.retirerPlaces(1L, 10));
        // Verified that findById is called once by the first getDisciplineOrThrow
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(0)).decrementerPlaces(anyLong(), anyInt());
    }

    @Test
    void retirerPlaces_InvalidNb_ThrowsIllegalArgumentException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(dummyDiscipline));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> disciplineService.retirerPlaces(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> disciplineService.retirerPlaces(1L, -50));
        // findById is called once for each call to retirerPlaces before the exception is thrown
        verify(disciplineRepository, times(2)).findById(1L);
        verify(disciplineRepository, times(0)).decrementerPlaces(anyLong(), anyInt());
    }

    @Test
    void retirerPlaces_NoRowsUpdated_ThrowsIllegalStateException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(dummyDiscipline));
        when(disciplineRepository.decrementerPlaces(1L, 10)).thenReturn(0); // Indicates no rows updated

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> disciplineService.retirerPlaces(1L, 10));
        // Verified that findById is called once by the first getDisciplineOrThrow
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(1)).decrementerPlaces(1L, 10);
    }


    @Test
    void ajouterPlaces_Successful() {
        // Arrange
        Discipline disciplineBeforeUpdate = new Discipline();
        disciplineBeforeUpdate.setIdDiscipline(1L);
        disciplineBeforeUpdate.setNbPlaceDispo(100);
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(disciplineBeforeUpdate));
        when(disciplineRepository.save(any(Discipline.class))).thenReturn(disciplineBeforeUpdate); // Returns the updated entity

        // Act
        Discipline updatedDiscipline = disciplineService.ajouterPlaces(1L, 50);

        // Assert
        assertNotNull(updatedDiscipline);
        assertEquals(150, updatedDiscipline.getNbPlaceDispo());
        // Verified that findById is called once by getDisciplineOrThrow
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(1)).save(any(Discipline.class));
    }

    @Test
    void ajouterPlaces_DisciplineNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> disciplineService.ajouterPlaces(1L, 50));
        // Verified that findById is called once by getDisciplineOrThrow
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void ajouterPlaces_InvalidNb_ThrowsIllegalArgumentException() {
        // Arrange
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(dummyDiscipline));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> disciplineService.ajouterPlaces(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> disciplineService.ajouterPlaces(1L, -50));
        // findById is called once for each call to ajouterPlaces before the exception is thrown
        verify(disciplineRepository, times(2)).findById(1L);
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void updateDate_Successful() {
        // Arrange
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        when(disciplineRepository.findById(1L)).thenReturn(Optional.of(dummyDiscipline));
        when(disciplineRepository.save(any(Discipline.class))).thenReturn(dummyDiscipline);

        // Act
        Discipline updatedDiscipline = disciplineService.updateDate(1L, futureDate);

        // Assert
        assertNotNull(updatedDiscipline);
        assertEquals(futureDate, updatedDiscipline.getDateDiscipline());
        // Verified that findById is called once by getDisciplineOrThrow
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(1)).save(any(Discipline.class));
    }

    @Test
    void updateDate_DisciplineNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        // Mock findById to return empty only when called with the specific ID
        when(disciplineRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        // Note: The date check passes here, so getDisciplineOrThrow is called
        assertThrows(EntityNotFoundException.class, () -> disciplineService.updateDate(1L, futureDate));
        // Verified that findById is called once by getDisciplineOrThrow before the EntityNotFoundException
        verify(disciplineRepository, times(1)).findById(1L);
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void updateDate_DateInPast_ThrowsIllegalArgumentException() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        // We don't need to mock findById here because it's never called when the date is in the past.

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> disciplineService.updateDate(1L, pastDate));
        // Verified that findById was NOT called because the IllegalArgumentException was thrown first
        verify(disciplineRepository, times(0)).findById(anyLong()); // Use anyLong() as findById(1L) is never called
        verify(disciplineRepository, times(0)).save(any(Discipline.class));
    }

    @Test
    void getDisciplinesAvenir_Successful() {
        // Arrange
        List<Discipline> futureDisciplines = Collections.singletonList(dummyDiscipline);
        when(disciplineRepository.findByDateDisciplineAfter(any(LocalDateTime.class))).thenReturn(futureDisciplines);

        // Act
        List<Discipline> result = disciplineService.getDisciplinesAvenir();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(dummyDiscipline, result.getFirst());
        verify(disciplineRepository, times(1)).findByDateDisciplineAfter(any(LocalDateTime.class));
    }

    @Test
    void findDisciplinesFiltered_ByVille() {
        // Arrange
        List<Discipline> disciplinesInVille = Collections.singletonList(dummyDiscipline);
        String ville = "Paris";
        when(disciplineRepository.findDisciplinesByVille(ville)).thenReturn(disciplinesInVille);

        // Act
        List<Discipline> result = disciplineService.findDisciplinesFiltered(ville, null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(dummyDiscipline, result.getFirst());
        verify(disciplineRepository, times(1)).findDisciplinesByVille(ville);
        verify(disciplineRepository, times(0)).findDisciplinesByDateDiscipline(any(LocalDateTime.class));
        verify(disciplineRepository, times(0)).findDisciplinesByEpreuveId(anyLong());
        verify(disciplineRepository, times(0)).findAll();
    }

    @Test
    void findDisciplinesFiltered_ByDate() {
        // Arrange
        List<Discipline> disciplinesOnDate = Collections.singletonList(dummyDiscipline);
        LocalDateTime date = LocalDateTime.now();
        when(disciplineRepository.findDisciplinesByDateDiscipline(date)).thenReturn(disciplinesOnDate);

        // Act
        List<Discipline> result = disciplineService.findDisciplinesFiltered(null, date, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(dummyDiscipline, result.getFirst());
        verify(disciplineRepository, times(0)).findDisciplinesByVille(anyString());
        verify(disciplineRepository, times(1)).findDisciplinesByDateDiscipline(date);
        verify(disciplineRepository, times(0)).findDisciplinesByEpreuveId(anyLong());
        verify(disciplineRepository, times(0)).findAll();
    }

    @Test
    void findDisciplinesFiltered_ByEpreuveId() {
        // Arrange
        List<Discipline> disciplinesForEpreuve = Collections.singletonList(dummyDiscipline);
        Long epreuveId = 10L;
        when(disciplineRepository.findDisciplinesByEpreuveId(epreuveId)).thenReturn(disciplinesForEpreuve);

        // Act
        List<Discipline> result = disciplineService.findDisciplinesFiltered(null, null, epreuveId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(dummyDiscipline, result.getFirst());
        verify(disciplineRepository, times(0)).findDisciplinesByVille(anyString());
        verify(disciplineRepository, times(0)).findDisciplinesByDateDiscipline(any(LocalDateTime.class));
        verify(disciplineRepository, times(1)).findDisciplinesByEpreuveId(epreuveId);
        verify(disciplineRepository, times(0)).findAll();
    }

    @Test
    void findDisciplinesFiltered_NoFilters() {
        // Arrange
        List<Discipline> allDisciplines = Collections.singletonList(dummyDiscipline);
        when(disciplineRepository.findAll()).thenReturn(allDisciplines);

        // Act
        List<Discipline> result = disciplineService.findDisciplinesFiltered(null, null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(dummyDiscipline, result.getFirst());
        verify(disciplineRepository, times(0)).findDisciplinesByVille(anyString());
        verify(disciplineRepository, times(0)).findDisciplinesByDateDiscipline(any(LocalDateTime.class));
        verify(disciplineRepository, times(0)).findDisciplinesByEpreuveId(anyLong());
        verify(disciplineRepository, times(1)).findAll();
    }

    @Test
    void getDisciplinesEnVedette_Successful() {
        // Arrange
        Epreuve epreuveEnVedette1 = new Epreuve();
        epreuveEnVedette1.setIdEpreuve(1L);
        Epreuve epreuveEnVedette2 = new Epreuve();
        epreuveEnVedette2.setIdEpreuve(2L);

        Discipline discipline1 = new Discipline();
        discipline1.setIdDiscipline(1L);
        discipline1.setNomDiscipline("Danse");

        Discipline discipline2 = new Discipline();
        discipline2.setIdDiscipline(2L);
        discipline2.setNomDiscipline("Boxe");

        Comporter comporteur1 = new Comporter();
        comporteur1.setDiscipline(discipline1);
        comporteur1.setEpreuve(epreuveEnVedette1);

        Comporter comporteur2 = new Comporter();
        comporteur2.setDiscipline(discipline2);
        comporteur2.setEpreuve(epreuveEnVedette1);

        Comporter comporteur3 = new Comporter();
        comporteur3.setDiscipline(discipline1); // Same discipline as comporteur1
        comporteur3.setEpreuve(epreuveEnVedette2);

        epreuveEnVedette1.setComporters(new HashSet<>(Arrays.asList(comporteur1, comporteur2)));
        epreuveEnVedette2.setComporters(new HashSet<>(Collections.singletonList(comporteur3)));

        List<Epreuve> epreuvesEnVedetteList = Arrays.asList(epreuveEnVedette1, epreuveEnVedette2);

        when(epreuveService.getEpreuvesEnVedette()).thenReturn(epreuvesEnVedetteList);

        // Act
        Set<Discipline> result = disciplineService.getDisciplinesEnVedette();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(discipline1));
        assertTrue(result.contains(discipline2));
        verify(epreuveService, times(1)).getEpreuvesEnVedette();
    }

    @Test
    void getDisciplinesEnVedette_NoFeaturedEpreuves() {
        // Arrange
        List<Epreuve> emptyEpreuvesList = Collections.emptyList();
        when(epreuveService.getEpreuvesEnVedette()).thenReturn(emptyEpreuvesList);

        // Act
        Set<Discipline> result = disciplineService.getDisciplinesEnVedette();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(epreuveService, times(1)).getEpreuvesEnVedette();
    }
}