package fr.studi.bloc3jo2024.controller.offres;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private Pageable pageable; // Ajouté

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        pageable = PageRequest.of(0, 10);

        creerOffreDto = new CreerOffreDto(TypeOffre.SOLO, 10, BigDecimal.valueOf(100.0), now.plusDays(30), StatutOffre.DISPONIBLE, 100L, 1, false);
        offreAdminDto = new OffreAdminDto(1L, TypeOffre.SOLO, 10, BigDecimal.valueOf(100.0), now.plusDays(30), StatutOffre.DISPONIBLE, 1, 100L, 0, false);
        mettreAJourOffreDto = new MettreAJourOffreDto(TypeOffre.DUO, 15, BigDecimal.valueOf(120.0), now.plusDays(60), StatutOffre.DISPONIBLE, 101L, 2, true);
        offreDto = new OffreDto(1L, 100L, TypeOffre.SOLO, BigDecimal.valueOf(100.0), 1, StatutOffre.DISPONIBLE, now.plusDays(30), 10, false);
    }

    @Test
    void ajouterOffre_Success() {
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenReturn(offreAdminDto);
        ResponseEntity<OffreAdminDto> response = adminOffreController.ajouterOffre(creerOffreDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreAdminDto.getId(), response.getBody().getId());
        verify(adminOffreService, times(1)).ajouterOffre(any(CreerOffreDto.class));
    }

    @Test
    void ajouterOffre_IllegalArgumentException() {
        when(adminOffreService.ajouterOffre(any(CreerOffreDto.class))).thenThrow(IllegalArgumentException.class);
        ResponseEntity<OffreAdminDto> response = adminOffreController.ajouterOffre(creerOffreDto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getOffreParId_Admin_Success() {
        Long offreId = 1L;
        when(adminOffreService.obtenirOffreParId(offreId)).thenReturn(offreAdminDto);
        ResponseEntity<OffreAdminDto> response = adminOffreController.getOffreParId(offreId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(offreId, response.getBody().getId());
    }

    @Test
    void getOffreParId_Admin_ResourceNotFoundException() {
        Long offreId = 1L;
        when(adminOffreService.obtenirOffreParId(offreId)).thenThrow(ResourceNotFoundException.class);
        ResponseEntity<OffreAdminDto> response = adminOffreController.getOffreParId(offreId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void mettreAJourOffre_Success() {
        Long offreId = 1L;
        when(adminOffreService.mettreAJourOffre(offreId, mettreAJourOffreDto)).thenReturn(offreAdminDto);
        ResponseEntity<OffreAdminDto> response = adminOffreController.mettreAJourOffre(offreId, mettreAJourOffreDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    @Test
    void obtenirToutesLesOffres_Admin_Success() {
        Page<OffreAdminDto> offresPage = new PageImpl<>(Collections.singletonList(offreAdminDto), pageable, 1);
        when(adminOffreService.obtenirToutesLesOffres(any(Pageable.class))).thenReturn(offresPage);

        ResponseEntity<Page<OffreAdminDto>> response = adminOffreController.obtenirToutesLesOffres(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(adminOffreService, times(1)).obtenirToutesLesOffres(any(Pageable.class));
    }

    @Test
    void obtenirToutesLesOffresDisponibles_User_Success() {
        Page<OffreDto> offresPage = new PageImpl<>(Collections.singletonList(offreDto), pageable, 1);
        when(utilisateurOffreService.obtenirToutesLesOffresDisponibles(any(Pageable.class))).thenReturn(offresPage);

        ResponseEntity<Page<OffreDto>> response = utilisateurOffreController.obtenirToutesLesOffresDisponibles(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(utilisateurOffreService, times(1)).obtenirToutesLesOffresDisponibles(any(Pageable.class));
    }


    @Test
    void obtenirOffreDisponibleParId_User_Success() {
        Long offreId = 1L;
        when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenReturn(offreDto);
        ResponseEntity<OffreDto> response = utilisateurOffreController.obtenirOffreDisponibleParId(offreId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void obtenirOffreDisponibleParId_User_ResourceNotFoundException() {
        Long offreId = 1L;
        when(utilisateurOffreService.obtenirOffreDisponibleParId(offreId)).thenThrow(new ResourceNotFoundException("Not found"));
        ResponseEntity<OffreDto> response = utilisateurOffreController.obtenirOffreDisponibleParId(offreId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}