package fr.studi.bloc3jo2024.controller; // Le package devrait être fr.studi.bloc3jo2024.controller.offres si les contrôleurs y sont

import fr.studi.bloc3jo2024.controller.offres.AdminOffreController;
import fr.studi.bloc3jo2024.controller.offres.UtilisateurOffreController;
import fr.studi.bloc3jo2024.dto.offres.CreerOffreDto;
import fr.studi.bloc3jo2024.dto.offres.MettreAJourOffreDto;
import fr.studi.bloc3jo2024.dto.offres.OffreAdminDto;
import fr.studi.bloc3jo2024.dto.offres.OffreDto;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.offres.AdminOffreService;
import fr.studi.bloc3jo2024.service.offres.UtilisateurOffreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page; // Importer Page
import org.springframework.data.domain.PageImpl; // Importer PageImpl
import org.springframework.data.domain.PageRequest; // Importer PageRequest
import org.springframework.data.domain.Pageable; // Importer Pageable
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour les contrôleurs d'offres : AdminOffreController et UtilisateurOffreController.
 * Il est recommandé de séparer ces tests dans des fichiers distincts.
 */
@ExtendWith(MockitoExtension.class)
class OffreControllerTest {

    // Mocks pour les services
    @Mock
    private AdminOffreService adminOffreService;

    @Mock
    private UtilisateurOffreService utilisateurOffreService;

    // Instances des contrôleurs à tester
    @InjectMocks
    private AdminOffreController adminOffreController;

    @InjectMocks
    private UtilisateurOffreController utilisateurOffreController;

    // DTOs de test
    private CreerOffreDto creerOffreDto;
    private OffreAdminDto offreAdminDto;
    private MettreAJourOffreDto mettreAJourOffreDto;
    private OffreDto offreDto;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.now(); // Utiliser un temps fixe pour la cohérence des dates

        creerOffreDto = CreerOffreDto.builder()
                .typeOffre(TypeOffre.SOLO)
                .quantite(10)
                .prix(new BigDecimal("100.00"))
                .dateExpiration(fixedTime.plusDays(30))
                .statutOffre(StatutOffre.DISPONIBLE)
                .idDiscipline(100L)
                .capacite(1) // Assurez-vous que ce champ est bien un Integer/int dans le DTO
                .featured(false)
                .build();

        offreAdminDto = OffreAdminDto.builder()
                .id(1L)
                .typeOffre(TypeOffre.SOLO)
                .quantite(10)
                .prix(new BigDecimal("100.00"))
                .dateExpiration(fixedTime.plusDays(30)) // Sera l'effective date
                .statutOffre(StatutOffre.DISPONIBLE)
                .capacite(1) // Assurez-vous que ce champ est bien un int dans le DTO
                .idDiscipline(100L)
                .nombreDeVentes(0)
                .featured(false)
                .build();

        mettreAJourOffreDto = MettreAJourOffreDto.builder()
                .typeOffre(TypeOffre.DUO)
                .quantite(15)
                .prix(new BigDecimal("120.00"))
                .dateExpiration(fixedTime.plusDays(60))
                .statutOffre(StatutOffre.DISPONIBLE)
                .idDiscipline(101L)
                .capacite(2) // Assurez-vous que ce champ est bien un Integer/int dans le DTO
                .featured(true)
                .build();

        offreDto = OffreDto.builder()
                .id(1L)
                .idDiscipline(100L)
                .typeOffre(TypeOffre.SOLO)
                .prix(new BigDecimal("100.00"))
                .capacite(1) // Assurez-vous que ce champ est bien un int dans le DTO
                .statutOffre(StatutOffre.DISPONIBLE)
                .dateExpiration(fixedTime.plusDays(30)) // Sera l'effective date
                .quantiteDisponible(10)
                .featured(false)
                .build();
    }

    @Nested
    class AdminOffreControllerTests {

        @Test
        void ajouterOffre_Success() {
            when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenReturn(offreAdminDto);
            ResponseEntity<OffreAdminDto> response = adminOffreController.ajouterOffre(creerOffreDto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(offreAdminDto.getId(), response.getBody().getId());
            assertEquals(TypeOffre.SOLO, response.getBody().getTypeOffre());
            // ATTENTION ICI: Ligne 63 originale. Assurez-vous que getCapacite() dans OffreAdminDto retourne int.
            assertEquals(1, response.getBody().getCapacite());
            assertFalse(response.getBody().isFeatured());
            verify(adminOffreService, times(1)).ajouterOffre(any(CreerOffreDto.class));
        }

        @Test
        void ajouterOffre_IllegalArgumentException_shouldBeHandledByGlobalHandler() {
            // Le contrôleur ne devrait plus avoir de try-catch pour IllegalArgumentException
            // si un GlobalExceptionHandler est en place.
            // Ce test vérifie que le service est appelé. Le test MVC vérifierait le statut HTTP.
            when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenThrow(new IllegalArgumentException("Test error"));

            // S'attendre à ce que l'exception du service soit propagée (pour être attrapée par le handler global)
            assertThrows(IllegalArgumentException.class, () -> adminOffreController.ajouterOffre(creerOffreDto));
            verify(adminOffreService, times(1)).ajouterOffre(any(CreerOffreDto.class));
        }

        @Test
        void obtenirOffreParId_Success() {
            Long offreId = 1L;
            when(adminOffreService.obtenirOffreParId(offreId)).thenReturn(offreAdminDto);
            ResponseEntity<OffreAdminDto> response = adminOffreController.obtenirOffreParId(offreId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(offreAdminDto, response.getBody());
            verify(adminOffreService, times(1)).obtenirOffreParId(offreId);
        }

        @Test
        void obtenirOffreParId_ResourceNotFoundException_shouldBeHandledByGlobalHandler() {
            Long offreId = 1L;
            when(adminOffreService.obtenirOffreParId(offreId)).thenThrow(new ResourceNotFoundException("Not found"));
            assertThrows(ResourceNotFoundException.class, () -> adminOffreController.obtenirOffreParId(offreId));
            verify(adminOffreService, times(1)).obtenirOffreParId(offreId);
        }

        @Test
        void mettreAJourOffre_Success() {
            Long offreId = 1L;
            when(adminOffreService.mettreAJourOffre(eq(offreId), any(MettreAJourOffreDto.class))).thenReturn(offreAdminDto);
            ResponseEntity<OffreAdminDto> response = adminOffreController.mettreAJourOffre(offreId, mettreAJourOffreDto);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(offreAdminDto, response.getBody());
            verify(adminOffreService, times(1)).mettreAJourOffre(eq(offreId), any(MettreAJourOffreDto.class));
        }

        @Test
        void supprimerOffre_Success() {
            Long offreId = 1L;
            doNothing().when(adminOffreService).supprimerOffre(offreId);
            ResponseEntity<Void> response = adminOffreController.supprimerOffre(offreId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(adminOffreService, times(1)).supprimerOffre(offreId);
        }

        @Test
        void obtenirToutesLesOffres_Success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10); // Créer un objet Pageable
            List<OffreAdminDto> offresList = Collections.singletonList(offreAdminDto);
            Page<OffreAdminDto> offresPage = new PageImpl<>(offresList, pageable, offresList.size());
            // Le service attend maintenant un Pageable
            when(adminOffreService.obtenirToutesLesOffres(any(Pageable.class))).thenReturn(offresPage);

            // Act
            // Le contrôleur doit maintenant accepter Pageable
            ResponseEntity<Page<OffreAdminDto>> response = adminOffreController.obtenirToutesLesOffres(pageable);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(offreAdminDto.getId(), response.getBody().getContent().get(0).getId());
            verify(adminOffreService, times(1)).obtenirToutesLesOffres(any(Pageable.class));
        }

        @Test
        void getVentesParOffre_Success() {
            Map<Long, Long> ventes = new HashMap<>();
            ventes.put(1L, 100L);
            when(adminOffreService.getNombreDeVentesParOffre()).thenReturn(ventes);
            ResponseEntity<Map<Long, Long>> response = adminOffreController.getVentesParOffre();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(ventes, response.getBody());
            verify(adminOffreService, times(1)).getNombreDeVentesParOffre();
        }

        @Test
        void getVentesParType_Success() {
            Map<String, Long> ventes = new HashMap<>();
            ventes.put(TypeOffre.SOLO.name(), 50L);
            when(adminOffreService.getVentesParTypeOffre()).thenReturn(ventes);
            ResponseEntity<Map<String, Long>> response = adminOffreController.getVentesParType();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(ventes, response.getBody());
            verify(adminOffreService, times(1)).getVentesParTypeOffre();
        }
    }

    @Nested
    class UtilisateurOffreControllerTests {

        @Test
        void obtenirToutesLesOffresDisponibles_Success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10); // Créer un objet Pageable
            List<OffreDto> offresList = Collections.singletonList(offreDto);
            Page<OffreDto> offresPage = new PageImpl<>(offresList, pageable, offresList.size());
            // Le service attend maintenant un Pageable
            when(utilisateurOffreService.obtenirToutesLesOffresDisponibles(any(Pageable.class))).thenReturn(offresPage);

            // Act
            // Le contrôleur doit maintenant accepter Pageable
            ResponseEntity<Page<OffreDto>> response = utilisateurOffreController.obtenirToutesLesOffresDisponibles(pageable);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(offreDto.getId(), response.getBody().getContent().get(0).getId());
            verify(utilisateurOffreService, times(1)).obtenirToutesLesOffresDisponibles(any(Pageable.class));
        }

        @Test
        void obtenirOffreDisponibleParId_Success() {
            Long offreId = 1L;
            when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenReturn(offreDto);
            ResponseEntity<OffreDto> response = utilisateurOffreController.obtenirOffreDisponibleParId(offreId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(offreDto, response.getBody());
            verify(utilisateurOffreService, times(1)).obtenirOffreDisponibleParId(offreId);
        }

        @Test
        void obtenirOffreDisponibleParId_ResourceNotFoundException_shouldBeHandledByGlobalHandler() {
            Long offreId = 1L;
            when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenThrow(new ResourceNotFoundException("Not found"));
            assertThrows(ResourceNotFoundException.class, () -> utilisateurOffreController.obtenirOffreDisponibleParId(offreId));
            verify(utilisateurOffreService, times(1)).obtenirOffreDisponibleParId(offreId);
        }
    }
}
