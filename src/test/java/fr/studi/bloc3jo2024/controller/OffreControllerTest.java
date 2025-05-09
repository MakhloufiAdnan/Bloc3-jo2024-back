package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.controller.offres.AdminOffreController;
import fr.studi.bloc3jo2024.controller.offres.UtilisateurOffreController;
import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreControllerTest {

    @Mock
    private AdminOffreService adminOffreService;

    @Mock
    private UtilisateurOffreService utilisateurOffreService;

    @InjectMocks
    private AdminOffreController adminOffreController;

    @InjectMocks
    private UtilisateurOffreController utilisateurOffreController;

    private CreerOffreDto creerOffreDto;
    private OffreAdminDto offreAdminDto;
    private MettreAJourOffreDto mettreAJourOffreDto;
    private OffreDto offreDto;

    @BeforeEach
    void setUp() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        creerOffreDto = new CreerOffreDto(TypeOffre.SOLO, 10, BigDecimal.valueOf(100.0), now.plusDays(30), StatutOffre.DISPONIBLE, 100L, 1, false);

        offreAdminDto = new OffreAdminDto(1L, TypeOffre.SOLO, 10, BigDecimal.valueOf(100.0), now.plusDays(30), StatutOffre.DISPONIBLE, 1, 100L, 0, false);

        mettreAJourOffreDto = new MettreAJourOffreDto(TypeOffre.DUO, 15, BigDecimal.valueOf(120.0), now.plusDays(60), StatutOffre.DISPONIBLE, 101L, 2, true);

        offreDto = new OffreDto(1L, 100L, TypeOffre.SOLO, BigDecimal.valueOf(100.0), 1, StatutOffre.DISPONIBLE, now.plusDays(30), 10, false);
    }

    // --- Tests AdminOffreController ---

    @Test
    void ajouterOffre_Success() {
        // Arrange
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenReturn(offreAdminDto);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.ajouterOffre(creerOffreDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreAdminDto.getId(), response.getBody().getId());
        // Vérifier les champs selon la structure de OffreAdminDto
        assertEquals(TypeOffre.SOLO, response.getBody().getTypeOffre());
        assertEquals(BigDecimal.valueOf(100.0), response.getBody().getPrix());
        assertEquals(10, response.getBody().getQuantite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getStatutOffre());
        assertEquals(1, response.getBody().getCapacite());
        assertEquals(100L, response.getBody().getIdDiscipline());
        assertFalse(response.getBody().isFeatured()); // Utilisation de assertFalse
        // On ne vérifie pas dateExpiration précisément car LocalDateTime.now() varie dans setUp vs test
        assertNotNull(response.getBody().getDateExpiration());
        verify(adminOffreService, times(1)).ajouterOffre(any(CreerOffreDto.class));
    }

    @Test
    void ajouterOffre_IllegalArgumentException() {
        // Arrange
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenThrow(IllegalArgumentException.class);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.ajouterOffre(creerOffreDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(adminOffreService, times(1)).ajouterOffre(any(CreerOffreDto.class));
    }

    @Test
    void obtenirOffreParId_Success() {
        // Arrange
        Long offreId = 1L;
        when(adminOffreService.obtenirOffreParId(offreId)).thenReturn(offreAdminDto);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.obtenirOffreParId(offreId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreId, response.getBody().getId());
        // Vérifier les champs selon la structure de OffreAdminDto
        assertEquals(TypeOffre.SOLO, response.getBody().getTypeOffre());
        assertEquals(BigDecimal.valueOf(100.0), response.getBody().getPrix());
        assertEquals(10, response.getBody().getQuantite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getStatutOffre());
        assertEquals(1, response.getBody().getCapacite());
        assertEquals(100L, response.getBody().getIdDiscipline());
        assertFalse(response.getBody().isFeatured()); // Utilisation de assertFalse
        assertNotNull(response.getBody().getDateExpiration());
        verify(adminOffreService, times(1)).obtenirOffreParId(offreId);
    }

    @Test
    void obtenirOffreParId_ResourceNotFoundException() {
        // Arrange
        Long offreId = 1L;
        when(adminOffreService.obtenirOffreParId(offreId)).thenThrow(ResourceNotFoundException.class);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.obtenirOffreParId(offreId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminOffreService, times(1)).obtenirOffreParId(offreId);
    }

    @Test
    void mettreAJourOffre_Success() {
        // Arrange
        Long offreId = 1L;
        LocalDateTime future = LocalDateTime.now().plusDays(60);
        OffreAdminDto updatedOffreAdminDto = new OffreAdminDto(offreId, TypeOffre.DUO, 15, BigDecimal.valueOf(120.0), future, StatutOffre.DISPONIBLE, 2, 101L, 5, true);
        when(adminOffreService.mettreAJourOffre(offreId, mettreAJourOffreDto)).thenReturn(updatedOffreAdminDto);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.mettreAJourOffre(offreId, mettreAJourOffreDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreId, response.getBody().getId());
        // Vérifier les champs mis à jour selon la structure de OffreAdminDto
        assertEquals(TypeOffre.DUO, response.getBody().getTypeOffre());
        assertEquals(BigDecimal.valueOf(120.0), response.getBody().getPrix());
        assertEquals(15, response.getBody().getQuantite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getStatutOffre());
        assertEquals(2, response.getBody().getCapacite());
        assertEquals(101L, response.getBody().getIdDiscipline());
        assertTrue(response.getBody().isFeatured()); // Utilisation de assertTrue
        // Comparaison de dateExpiration
        assertEquals(future, response.getBody().getDateExpiration());
        verify(adminOffreService, times(1)).mettreAJourOffre(offreId, mettreAJourOffreDto);
    }

    @Test
    void mettreAJourOffre_ResourceNotFoundException() {
        // Arrange
        Long offreId = 1L;
        when(adminOffreService.mettreAJourOffre(offreId, mettreAJourOffreDto)).thenThrow(ResourceNotFoundException.class);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.mettreAJourOffre(offreId, mettreAJourOffreDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminOffreService, times(1)).mettreAJourOffre(offreId, mettreAJourOffreDto);
    }

    @Test
    void mettreAJourOffre_IllegalArgumentException() {
        // Arrange
        Long offreId = 1L;
        when(adminOffreService.mettreAJourOffre(offreId, mettreAJourOffreDto)).thenThrow(IllegalArgumentException.class);

        // Act
        ResponseEntity<OffreAdminDto> response = adminOffreController.mettreAJourOffre(offreId, mettreAJourOffreDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(adminOffreService, times(1)).mettreAJourOffre(offreId, mettreAJourOffreDto);
    }

    @Test
    void supprimerOffre_Success() {
        // Arrange
        Long offreId = 1L;
        doNothing().when(adminOffreService).supprimerOffre(offreId);

        // Act
        ResponseEntity<Void> response = adminOffreController.supprimerOffre(offreId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminOffreService, times(1)).supprimerOffre(offreId);
    }

    @Test
    void supprimerOffre_ResourceNotFoundException() {
        // Arrange
        Long offreId = 1L;
        doThrow(ResourceNotFoundException.class).when(adminOffreService).supprimerOffre(offreId);

        // Act
        ResponseEntity<Void> response = adminOffreController.supprimerOffre(offreId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminOffreService, times(1)).supprimerOffre(offreId);
    }

    @Test
    void obtenirToutesLesOffres_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<OffreAdminDto> offres = Arrays.asList(
                offreAdminDto, // Utilise l'offreAdminDto de base
                // Nouvelle offre pour la liste avec statut EXPIRE
                new OffreAdminDto(2L, TypeOffre.FAMILIALE, 5, BigDecimal.valueOf(200.0), now.plusDays(90), StatutOffre.EXPIRE, 4, 102L, 10, true)
        );
        when(adminOffreService.obtenirToutesLesOffres()).thenReturn(offres);

        // Act
        ResponseEntity<List<OffreAdminDto>> response = adminOffreController.obtenirToutesLesOffres();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Utilisation de get(index) pour accéder aux éléments

        // Vérifier les champs du premier élément (offreAdminDto)
        assertEquals(offreAdminDto.getId(), response.getBody().getFirst().getId());
        assertEquals(TypeOffre.SOLO, response.getBody().getFirst().getTypeOffre());
        assertEquals(BigDecimal.valueOf(100.0), response.getBody().getFirst().getPrix());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getFirst().getStatutOffre());

        // Vérifier les champs du deuxième élément
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals(TypeOffre.FAMILIALE, response.getBody().get(1).getTypeOffre());
        assertEquals(BigDecimal.valueOf(200.0), response.getBody().get(1).getPrix());
        assertEquals(StatutOffre.EXPIRE, response.getBody().get(1).getStatutOffre());
        assertTrue(response.getBody().get(1).isFeatured()); // Utilisation de assertTrue

        verify(adminOffreService, times(1)).obtenirToutesLesOffres();
    }

    @Test
    void getVentesParOffre_Success() {
        // Arrange
        Map<Long, Long> ventesParOffre = new HashMap<>();
        ventesParOffre.put(1L, 10L);
        ventesParOffre.put(2L, 5L);
        when(adminOffreService.getNombreDeVentesParOffre()).thenReturn(ventesParOffre);

        // Act
        ResponseEntity<Map<Long, Long>> response = adminOffreController.getVentesParOffre();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(10L, response.getBody().get(1L));
        verify(adminOffreService, times(1)).getNombreDeVentesParOffre();
    }

    @Test
    void getVentesParType_Success() {
        // Arrange
        Map<String, Long> ventesParType = new HashMap<>();
        ventesParType.put(TypeOffre.SOLO.name(), 15L);
        ventesParType.put(TypeOffre.DUO.name(), 8L);
        when(adminOffreService.getVentesParTypeOffre()).thenReturn(ventesParType);

        // Act
        ResponseEntity<Map<String, Long>> response = adminOffreController.getVentesParType();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(15L, response.getBody().get(TypeOffre.SOLO.name()));
        verify(adminOffreService, times(1)).getVentesParTypeOffre();
    }

    // --- Tests UtilisateurOffreController ---

    @Test
    void obtenirToutesLesOffresDisponibles_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<OffreDto> offres = Arrays.asList(
                offreDto, // Utilise l'offreDto de base
                // Nouvelle offre pour la liste
                new OffreDto(2L, 101L, TypeOffre.DUO, BigDecimal.valueOf(250.0), 2, StatutOffre.DISPONIBLE, now.plusDays(75), 5, true)
        );
        when(utilisateurOffreService.obtenirToutesLesOffresDisponibles()).thenReturn(offres);

        // Act
        ResponseEntity<List<OffreDto>> response = utilisateurOffreController.obtenirToutesLesOffresDisponibles();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Vérifier les champs du premier élément (offreDto)
        assertEquals(offreDto.getId(), response.getBody().getFirst().getId());
        assertEquals(100L, response.getBody().getFirst().getIdDiscipline());
        assertEquals(TypeOffre.SOLO, response.getBody().getFirst().getTypeOffre());
        assertEquals(BigDecimal.valueOf(100.0), response.getBody().getFirst().getPrix());
        assertEquals(1, response.getBody().getFirst().getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getFirst().getStatutOffre());
        assertNotNull(response.getBody().getFirst().getDateExpiration());
        assertEquals(10, response.getBody().getFirst().getQuantiteDisponible());
        assertFalse(response.getBody().getFirst().isFeatured()); // Utilisation de assertFalse

        // Vérifier les champs du deuxième élément
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals(101L, response.getBody().get(1).getIdDiscipline());
        assertEquals(TypeOffre.DUO, response.getBody().get(1).getTypeOffre());
        assertEquals(BigDecimal.valueOf(250.0), response.getBody().get(1).getPrix());
        assertEquals(2, response.getBody().get(1).getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().get(1).getStatutOffre());
        assertNotNull(response.getBody().get(1).getDateExpiration());
        assertEquals(5, response.getBody().get(1).getQuantiteDisponible());
        assertTrue(response.getBody().get(1).isFeatured()); // Utilisation de assertTrue

        verify(utilisateurOffreService, times(1)).obtenirToutesLesOffresDisponibles();
    }

    @Test
    void obtenirOffreDisponibleParId_Success() {
        // Arrange
        Long offreId = 1L;
        when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenReturn(offreDto);

        // Act
        ResponseEntity<OffreDto> response = utilisateurOffreController.obtenirOffreDisponibleParId(offreId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreId, response.getBody().getId());
        // Vérifier les champs selon la structure OffreDto
        assertEquals(100L, response.getBody().getIdDiscipline());
        assertEquals(TypeOffre.SOLO, response.getBody().getTypeOffre());
        assertEquals(BigDecimal.valueOf(100.0), response.getBody().getPrix());
        assertEquals(1, response.getBody().getCapacite());
        assertEquals(StatutOffre.DISPONIBLE, response.getBody().getStatutOffre());
        assertNotNull(response.getBody().getDateExpiration());
        assertEquals(10, response.getBody().getQuantiteDisponible());
        assertFalse(response.getBody().isFeatured()); // Utilisation de assertFalse
        verify(utilisateurOffreService, times(1)).obtenirOffreDisponibleParId(offreId);
    }

    @Test
    void obtenirOffreDisponibleParId_Exception() {
        // Arrange
        Long offreId = 1L;
        // Simuler une exception générique (ici, un RuntimeException)
        when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenThrow(new RuntimeException("Erreur simulée"));

        // Act
        ResponseEntity<OffreDto> response = utilisateurOffreController.obtenirOffreDisponibleParId(offreId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(utilisateurOffreService, times(1)).obtenirOffreDisponibleParId(offreId);
    }
}