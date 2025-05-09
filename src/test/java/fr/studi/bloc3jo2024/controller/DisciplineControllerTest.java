package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.disciplines.CreerDisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.disciplines.MettreAJourDisciplineDto;
import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.service.DisciplineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisciplineControllerTest {

    @Mock
    private DisciplineService disciplineService;

    @InjectMocks
    private DisciplineController disciplineController;

    private Discipline disciplineExemple1;
    private Discipline disciplineExemple2;
    private DisciplineDto disciplineDtoExemple1;
    private DisciplineDto disciplineDtoExemple2;
    private CreerDisciplineDto creerDisciplineDtoExemple;
    private MettreAJourDisciplineDto mettreAJourDisciplineDtoExemple;
    private Adresse adresseExemple;

    @BeforeEach
    void setUp() {
        // Arrange

        adresseExemple = new Adresse();
        adresseExemple.setIdAdresse(100L);

        disciplineExemple1 = new Discipline();
        disciplineExemple1.setIdDiscipline(1L);
        disciplineExemple1.setNomDiscipline("Natation");
        disciplineExemple1.setDateDiscipline(LocalDateTime.of(2025, 7, 26, 10, 0));
        disciplineExemple1.setNbPlaceDispo(50);
        disciplineExemple1.setAdresse(adresseExemple);

        disciplineExemple2 = new Discipline();
        disciplineExemple2.setIdDiscipline(2L);
        disciplineExemple2.setNomDiscipline("Athlétisme");
        disciplineExemple2.setDateDiscipline(LocalDateTime.of(2025, 7, 27, 14, 0));
        disciplineExemple2.setNbPlaceDispo(100);
        disciplineExemple2.setAdresse(adresseExemple);

        // Créer les DTOs correspondants
        disciplineDtoExemple1 = convertToDto(disciplineExemple1);
        disciplineDtoExemple2 = convertToDto(disciplineExemple2);

        creerDisciplineDtoExemple = new CreerDisciplineDto();
        creerDisciplineDtoExemple.setNomDiscipline("Nouvelle Discipline");
        creerDisciplineDtoExemple.setDateDiscipline(LocalDateTime.of(2025, 8, 1, 9, 0));
        creerDisciplineDtoExemple.setNbPlaceDispo(20);
        creerDisciplineDtoExemple.setIdAdresse(101L);

        mettreAJourDisciplineDtoExemple = new MettreAJourDisciplineDto();
        mettreAJourDisciplineDtoExemple.setIdDiscipline(1L);
        mettreAJourDisciplineDtoExemple.setNomDiscipline("Natation - Mise à jour");
        mettreAJourDisciplineDtoExemple.setNbPlaceDispo(60);
        mettreAJourDisciplineDtoExemple.setIdAdresse(102L);
        mettreAJourDisciplineDtoExemple.setDateDiscipline(LocalDateTime.of(2025, 7, 26, 11, 0));
    }

    // Méthode helper pour simuler la conversion Entité -> DTO du contrôleur
    private DisciplineDto convertToDto(Discipline discipline) {
        if (discipline == null) {
            return null;
        }
        Long adresseId = (discipline.getAdresse() != null) ? discipline.getAdresse().getIdAdresse() : null;
        return new DisciplineDto(
                discipline.getIdDiscipline(),
                discipline.getNomDiscipline(),
                discipline.getDateDiscipline(),
                discipline.getNbPlaceDispo(),
                adresseId
        );
    }

    // --- Méthodes helpers pour créer des mocks d'entités ---

    private Discipline createMockCreatedDiscipline(CreerDisciplineDto dto) {
        Discipline disciplineCreee = new Discipline();
        disciplineCreee.setIdDiscipline(1L);
        disciplineCreee.setNomDiscipline(dto.getNomDiscipline());
        disciplineCreee.setDateDiscipline(dto.getDateDiscipline());
        disciplineCreee.setNbPlaceDispo(dto.getNbPlaceDispo());

        Adresse adresse = new Adresse();
        adresse.setIdAdresse(dto.getIdAdresse());
        disciplineCreee.setAdresse(adresse);

        return disciplineCreee;
    }

    private Discipline createMockUpdatedDiscipline(MettreAJourDisciplineDto dto) {
        Discipline disciplineMiseAJour = new Discipline();
        disciplineMiseAJour.setIdDiscipline(dto.getIdDiscipline());
        disciplineMiseAJour.setNomDiscipline(dto.getNomDiscipline());
        disciplineMiseAJour.setNbPlaceDispo(dto.getNbPlaceDispo());
        disciplineMiseAJour.setDateDiscipline(dto.getDateDiscipline());

        Adresse adresse = new Adresse();
        adresse.setIdAdresse(dto.getIdAdresse());
        disciplineMiseAJour.setAdresse(adresse);

        return disciplineMiseAJour;
    }

    // --- Tests Endpoint GET /disciplines (getDisciplines) ---

    @Test
    void getDisciplines_SansFiltre_RetourneListeCorrecte() {
        // Arrange
        List<Discipline> disciplines = Arrays.asList(disciplineExemple1, disciplineExemple2);
        when(disciplineService.findDisciplinesFiltered(isNull(), isNull(), isNull())).thenReturn(disciplines);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplines(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(disciplineDtoExemple1.getIdDiscipline(), result.getFirst().getIdDiscipline());
        assertEquals(disciplineDtoExemple2.getIdDiscipline(), result.get(1).getIdDiscipline());

        verify(disciplineService, times(1)).findDisciplinesFiltered(isNull(), isNull(), isNull());
    }

    @Test
    void getDisciplines_AvecFiltreVille_RetourneListeCorrecte() {
        // Arrange
        String ville = "Paris";
        List<Discipline> disciplines = Collections.singletonList(disciplineExemple1);
        when(disciplineService.findDisciplinesFiltered(eq(ville), isNull(), isNull())).thenReturn(disciplines);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplines(ville, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(disciplineDtoExemple1.getIdDiscipline(), result.getFirst().getIdDiscipline());

        verify(disciplineService, times(1)).findDisciplinesFiltered(eq(ville), isNull(), isNull());
    }

    @Test
    void getDisciplines_RetourneListeVideSiAucune() {
        // Arrange
        List<Discipline> disciplines = Collections.emptyList();
        when(disciplineService.findDisciplinesFiltered(any(), any(), any())).thenReturn(disciplines);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplines(null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(disciplineService, times(1)).findDisciplinesFiltered(any(), any(), any());
    }

    // --- Tests Endpoint GET /disciplines/avenir (getDisciplinesAvenir) ---

    @Test
    void getDisciplinesAvenir_RetourneListeCorrecte() {
        // Arrange
        List<Discipline> disciplinesAvenir = Arrays.asList(disciplineExemple1, disciplineExemple2);
        when(disciplineService.getDisciplinesAvenir()).thenReturn(disciplinesAvenir);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplinesAvenir();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(disciplineDtoExemple1.getIdDiscipline(), result.getFirst().getIdDiscipline());
        assertEquals(disciplineDtoExemple2.getIdDiscipline(), result.get(1).getIdDiscipline());

        verify(disciplineService, times(1)).getDisciplinesAvenir();
    }

    @Test
    void getDisciplinesAvenir_RetourneListeVideSiAucune() {
        // Arrange
        List<Discipline> disciplinesAvenir = Collections.emptyList();
        when(disciplineService.getDisciplinesAvenir()).thenReturn(disciplinesAvenir);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplinesAvenir();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(disciplineService, times(1)).getDisciplinesAvenir();
    }

    // --- Tests Endpoint GET /disciplines/vedette (getDisciplinesEnVedette) ---

    @Test
    void getDisciplinesEnVedette_RetourneListeCorrecte() {
        // Arrange
        Set<Discipline> disciplinesEnVedette = Set.of(disciplineExemple2);
        when(disciplineService.getDisciplinesEnVedette()).thenReturn(disciplinesEnVedette);
        // Act
        List<DisciplineDto> result = disciplineController.getDisciplinesEnVedette();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(disciplineDtoExemple2.getIdDiscipline(), result.getFirst().getIdDiscipline());

        verify(disciplineService, times(1)).getDisciplinesEnVedette();
    }

    @Test
    void getDisciplinesEnVedette_RetourneListeVideSiAucune() {
        // Arrange
        Set<Discipline> disciplinesEnVedette = Collections.emptySet();
        when(disciplineService.getDisciplinesEnVedette()).thenReturn(disciplinesEnVedette);

        // Act
        List<DisciplineDto> result = disciplineController.getDisciplinesEnVedette();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(disciplineService, times(1)).getDisciplinesEnVedette();
    }

    // --- Tests Endpoint POST /disciplines (creerDiscipline) ---

    @Test
    void creerDiscipline_Success() {
        // Arrange
        Discipline disciplineCreee = createMockCreatedDiscipline(creerDisciplineDtoExemple);

        when(disciplineService.creerDiscipline(any(CreerDisciplineDto.class))).thenReturn(disciplineCreee);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.creerDiscipline(creerDisciplineDtoExemple);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        DisciplineDto returnedDto = response.getBody();
        assertEquals(disciplineCreee.getIdDiscipline(), returnedDto.getIdDiscipline());
        assertEquals(disciplineCreee.getNomDiscipline(), returnedDto.getNomDiscipline());
        assertEquals(disciplineCreee.getDateDiscipline(), returnedDto.getDateDiscipline());
        assertEquals(disciplineCreee.getNbPlaceDispo(), returnedDto.getNbPlaceDispo());
        assertEquals(disciplineCreee.getAdresse().getIdAdresse(), returnedDto.getIdAdresse());

        verify(disciplineService, times(1)).creerDiscipline(creerDisciplineDtoExemple);
    }

    // --- Tests Endpoint PUT /disciplines/{id} (mettreAJourDiscipline) ---

    @Test
    void mettreAJourDiscipline_Success_IdMatch() {
        // Arrange
        Long disciplineId = 1L;
        mettreAJourDisciplineDtoExemple.setIdDiscipline(disciplineId);

        Discipline disciplineMiseAJour = createMockUpdatedDiscipline(mettreAJourDisciplineDtoExemple);

        when(disciplineService.mettreAJourDiscipline(any(MettreAJourDisciplineDto.class))).thenReturn(disciplineMiseAJour);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.mettreAJourDiscipline(disciplineId, mettreAJourDisciplineDtoExemple);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        DisciplineDto returnedDto = response.getBody();
        assertEquals(disciplineMiseAJour.getIdDiscipline(), returnedDto.getIdDiscipline());
        assertEquals(disciplineMiseAJour.getNomDiscipline(), returnedDto.getNomDiscipline());
        assertEquals(disciplineMiseAJour.getDateDiscipline(), returnedDto.getDateDiscipline());
        assertEquals(disciplineMiseAJour.getNbPlaceDispo(), returnedDto.getNbPlaceDispo());
        assertEquals(disciplineMiseAJour.getAdresse().getIdAdresse(), returnedDto.getIdAdresse());

        verify(disciplineService, times(1)).mettreAJourDiscipline(mettreAJourDisciplineDtoExemple);
    }

    @Test
    void mettreAJourDiscipline_BadRequest_IdMismatch() {
        // Arrange
        Long disciplineIdPathVariable = 1L;
        Long disciplineIdDto = 2L;

        mettreAJourDisciplineDtoExemple.setIdDiscipline(disciplineIdDto);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.mettreAJourDiscipline(disciplineIdPathVariable, mettreAJourDisciplineDtoExemple);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(disciplineService, never()).mettreAJourDiscipline(any(MettreAJourDisciplineDto.class));
    }

    // Note : Les tests pour ResourceNotFoundException (si le service les lance) dépendent de la gestion du contrôleur.


    // --- Tests Endpoint DELETE /disciplines/{id} (supprimerDiscipline) ---

    @Test
    void supprimerDiscipline_Success() {
        // Arrange
        Long disciplineId = 1L;
        doNothing().when(disciplineService).supprimerDiscipline(disciplineId);

        // Act
        ResponseEntity<Void> response = disciplineController.supprimerDiscipline(disciplineId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(disciplineService, times(1)).supprimerDiscipline(disciplineId);
    }

    // Note : Comme pour PUT, les tests pour ResourceNotFoundException (si le service les lance)
    // dépendent de la gestion d'exception dans le contrôleur.


    // --- Tests Endpoint PATCH /disciplines/{id}/retirer-places (retirerPlaces) ---

    @Test
    void retirerPlaces_Success() {
        // Arrange
        Long disciplineId = 1L;
        int nbPlacesARetirer = 5;

        Discipline disciplineMaj = new Discipline();
        disciplineMaj.setIdDiscipline(disciplineId);
        disciplineMaj.setNbPlaceDispo(disciplineExemple1.getNbPlaceDispo() - nbPlacesARetirer);
        disciplineMaj.setAdresse(adresseExemple);

        when(disciplineService.retirerPlaces(disciplineId, nbPlacesARetirer)).thenReturn(disciplineMaj);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.retirerPlaces(disciplineId, nbPlacesARetirer);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(disciplineMaj.getIdDiscipline(), response.getBody().getIdDiscipline());
        assertEquals(disciplineMaj.getNbPlaceDispo(), response.getBody().getNbPlaceDispo());
        assertEquals(disciplineMaj.getAdresse().getIdAdresse(), response.getBody().getIdAdresse());

        verify(disciplineService, times(1)).retirerPlaces(disciplineId, nbPlacesARetirer);
    }


    // --- Tests Endpoint PATCH /disciplines/{id}/ajouter-places (ajouterPlaces) ---

    @Test
    void ajouterPlaces_Success() {
        // Arrange
        Long disciplineId = 1L;
        int nbPlacesAAjouter = 10;

        Discipline disciplineMaj = new Discipline();
        disciplineMaj.setIdDiscipline(disciplineId);
        disciplineMaj.setNbPlaceDispo(disciplineExemple1.getNbPlaceDispo() + nbPlacesAAjouter);
        disciplineMaj.setAdresse(adresseExemple);

        when(disciplineService.ajouterPlaces(disciplineId, nbPlacesAAjouter)).thenReturn(disciplineMaj);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.ajouterPlaces(disciplineId, nbPlacesAAjouter);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(disciplineMaj.getIdDiscipline(), response.getBody().getIdDiscipline());
        assertEquals(disciplineMaj.getNbPlaceDispo(), response.getBody().getNbPlaceDispo());
        assertEquals(disciplineMaj.getAdresse().getIdAdresse(), response.getBody().getIdAdresse());

        verify(disciplineService, times(1)).ajouterPlaces(disciplineId, nbPlacesAAjouter);
    }

    // Note : Tests pour exceptions (ex: ResourceNotFoundException) dépendent de la gestion du contrôleur.


    // --- Tests Endpoint PATCH /disciplines/{id}/modifier-date (updateDate) ---

    @Test
    void updateDate_Success() {
        // Arrange
        Long disciplineId = 1L;
        LocalDateTime nouvelleDate = LocalDateTime.of(2025, 9, 1, 18, 0);

        Discipline disciplineMaj = new Discipline();
        disciplineMaj.setIdDiscipline(disciplineId);
        disciplineMaj.setDateDiscipline(nouvelleDate);
        disciplineMaj.setAdresse(adresseExemple);


        when(disciplineService.updateDate(disciplineId, nouvelleDate)).thenReturn(disciplineMaj);

        // Act
        ResponseEntity<DisciplineDto> response = disciplineController.updateDate(disciplineId, nouvelleDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(disciplineMaj.getIdDiscipline(), response.getBody().getIdDiscipline());
        assertEquals(disciplineMaj.getDateDiscipline(), response.getBody().getDateDiscipline());
        assertEquals(disciplineMaj.getAdresse().getIdAdresse(), response.getBody().getIdAdresse());

        verify(disciplineService, times(1)).updateDate(disciplineId, nouvelleDate);
    }

    // Note : Tests pour exceptions (ex: ResourceNotFoundException) dépendent de la gestion du contrôleur.
}