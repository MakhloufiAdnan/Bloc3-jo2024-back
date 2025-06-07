package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.epreuves.MettreAJourEpreuveVedetteDto;
import fr.studi.bloc3jo2024.entity.Epreuve;
import fr.studi.bloc3jo2024.service.EpreuveService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpreuveControllerTest {

    @Mock
    private EpreuveService epreuveService;

    @InjectMocks
    private EpreuveController epreuveController;

    private Epreuve epreuveExemple1;
    private Epreuve epreuveExemple2;
    private MettreAJourEpreuveVedetteDto mettreAJourEpreuveVedetteDto;

    @BeforeEach
    void setUp() {
        // Arrange

        epreuveExemple1 = new Epreuve();
        epreuveExemple1.setIdEpreuve(1L);
        epreuveExemple1.setNomEpreuve("Épreuve 1");
        epreuveExemple1.setFeatured(false);

        epreuveExemple2 = new Epreuve();
        epreuveExemple2.setIdEpreuve(2L);
        epreuveExemple2.setNomEpreuve("Épreuve 2");
        epreuveExemple2.setFeatured(true);

        mettreAJourEpreuveVedetteDto = new MettreAJourEpreuveVedetteDto();
        mettreAJourEpreuveVedetteDto.setIdEpreuve(1L);
        mettreAJourEpreuveVedetteDto.setIsFeatured(true);
    }

    // --- Tests Endpoint GET /admin/epreuves (getAllEpreuves) ---

    @Test
    void getAllEpreuves_Success() {
        // Arrange
        List<Epreuve> toutesLesEpreuves = Arrays.asList(epreuveExemple1, epreuveExemple2);
        when(epreuveService.getAllEpreuves()).thenReturn(toutesLesEpreuves);

        // Act
        ResponseEntity<List<Epreuve>> response = epreuveController.getAllEpreuves();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(epreuveExemple1.getIdEpreuve(), response.getBody().get(0).getIdEpreuve());
        assertEquals(epreuveExemple2.getIdEpreuve(), response.getBody().get(1).getIdEpreuve());
        verify(epreuveService, times(1)).getAllEpreuves();
    }

    @Test
    void getAllEpreuves_EmptyList() {
        // Arrange
        List<Epreuve> toutesLesEpreuves = Collections.emptyList();
        when(epreuveService.getAllEpreuves()).thenReturn(toutesLesEpreuves);

        // Act
        ResponseEntity<List<Epreuve>> response = epreuveController.getAllEpreuves();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(epreuveService, times(1)).getAllEpreuves();
    }

    // --- Tests Endpoint GET /admin/epreuves/vedette (getEpreuvesEnVedette) ---

    @Test
    void getEpreuvesEnVedette_Success() {
        // Arrange
        List<Epreuve> epreuvesVedetteList = Arrays.asList(epreuveExemple2);
        when(epreuveService.getEpreuvesEnVedette()).thenReturn(epreuvesVedetteList);

        // Act
        ResponseEntity<List<Epreuve>> response = epreuveController.getEpreuvesEnVedette();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(epreuveExemple2.getIdEpreuve(), response.getBody().getFirst().getIdEpreuve());
        assertTrue(response.getBody().getFirst().isFeatured());
        verify(epreuveService, times(1)).getEpreuvesEnVedette();
    }

    @Test
    void getEpreuvesEnVedette_EmptyList() {
        // Arrange
        List<Epreuve> epreuvesVedetteList = Collections.emptyList();
        when(epreuveService.getEpreuvesEnVedette()).thenReturn(epreuvesVedetteList);

        // Act
        ResponseEntity<List<Epreuve>> response = epreuveController.getEpreuvesEnVedette();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(epreuveService, times(1)).getEpreuvesEnVedette();
    }

    // --- Tests Endpoint PATCH /admin/epreuves/vedette (mettreAJourStatutVedette) ---

    @Test
    void mettreAJourStatutVedette_Success() {
        // Arrange
        Epreuve epreuveMiseAJour = new Epreuve();
        epreuveMiseAJour.setIdEpreuve(1L);
        epreuveMiseAJour.setNomEpreuve("Épreuve 1");
        epreuveMiseAJour.setFeatured(true);

        when(epreuveService.mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class))).thenReturn(epreuveMiseAJour);

        // Act
        ResponseEntity<Epreuve> response = epreuveController.mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(epreuveMiseAJour.getIdEpreuve(), response.getBody().getIdEpreuve());
        assertEquals(true, response.getBody().isFeatured());
        verify(epreuveService, times(1)).mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);
    }

    @Test
    void mettreAJourStatutVedette_NotFound() {
        // Arrange
        when(epreuveService.mettreAJourStatutVedette(any(MettreAJourEpreuveVedetteDto.class))).thenThrow(new EntityNotFoundException("Épreuve non trouvée"));

        // Act
        ResponseEntity<Epreuve> response = epreuveController.mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(epreuveService, times(1)).mettreAJourStatutVedette(mettreAJourEpreuveVedetteDto);
    }
}