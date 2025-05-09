package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.PanierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanierControllerTest {

    @InjectMocks
    private PanierController panierController;

    @Mock
    private PanierService panierService;

    private final UUID userId = UUID.randomUUID();
    private final Long offreId = 10L;
    private final AjouterOffrePanierDto ajouterOffreDto = new AjouterOffrePanierDto(offreId, 2);
    private final ModifierContenuPanierDto modifierQuantiteDto = new ModifierContenuPanierDto(offreId, 3);
    private final PanierDto panierDto = new PanierDto();

    @Test
    void getPanier_ExistingPanier_ReturnsOkPanierDto() {
        // Arrange
        when(panierService.getPanierUtilisateur(userId.toString())).thenReturn(panierDto);

        // Act
        ResponseEntity<PanierDto> response = panierController.getPanier(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(panierDto, response.getBody());
        verify(panierService, times(1)).getPanierUtilisateur(userId.toString());
    }

    @Test
    void getPanier_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(panierService.getPanierUtilisateur(userId.toString())).thenThrow(new ResourceNotFoundException("Panier non trouvé pour cet utilisateur"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.getPanier(userId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Panier non trouvé pour cet utilisateur", exception.getReason());
        verify(panierService, times(1)).getPanierUtilisateur(userId.toString());
    }

    @Test
    void ajouterOffre_ValidInput_ReturnsCreatedPanierDto() {
        // Arrange
        when(panierService.ajouterOffreAuPanier(userId.toString(), ajouterOffreDto)).thenReturn(panierDto);

        // Act
        ResponseEntity<PanierDto> response = panierController.ajouterOffre(userId, ajouterOffreDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(panierDto, response.getBody());
        verify(panierService, times(1)).ajouterOffreAuPanier(userId.toString(), ajouterOffreDto);
    }

    @Test
    void ajouterOffre_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(panierService.ajouterOffreAuPanier(userId.toString(), ajouterOffreDto)).thenThrow(new ResourceNotFoundException("Offre non trouvée"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.ajouterOffre(userId, ajouterOffreDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Offre non trouvée", exception.getReason());
        verify(panierService, times(1)).ajouterOffreAuPanier(userId.toString(), ajouterOffreDto);
    }

    @Test
    void ajouterOffre_IllegalArgument_ThrowsBadRequestException() {
        // Arrange
        when(panierService.ajouterOffreAuPanier(userId.toString(), ajouterOffreDto)).thenThrow(new IllegalArgumentException("Quantité invalide"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.ajouterOffre(userId, ajouterOffreDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Quantité invalide", exception.getReason());
        verify(panierService, times(1)).ajouterOffreAuPanier(userId.toString(), ajouterOffreDto);
    }

    @Test
    void ajouterOffre_IllegalState_ThrowsConflictException() {
        // Arrange
        when(panierService.ajouterOffreAuPanier(userId.toString(), ajouterOffreDto)).thenThrow(new IllegalStateException("Stock insuffisant pour cette offre"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.ajouterOffre(userId, ajouterOffreDto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Stock insuffisant pour cette offre", exception.getReason());
        verify(panierService, times(1)).ajouterOffreAuPanier(userId.toString(), ajouterOffreDto);
    }

    @Test
    void modifierQuantite_ValidInput_ReturnsOkPanierDto() {
        // Arrange
        when(panierService.modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto)).thenReturn(panierDto);

        // Act
        ResponseEntity<PanierDto> response = panierController.modifierQuantite(userId, modifierQuantiteDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(panierDto, response.getBody());
        verify(panierService, times(1)).modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto);
    }

    @Test
    void modifierQuantite_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(panierService.modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto)).thenThrow(new ResourceNotFoundException("Offre non trouvée dans le panier"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.modifierQuantite(userId, modifierQuantiteDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Offre non trouvée dans le panier", exception.getReason());
        verify(panierService, times(1)).modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto);
    }

    @Test
    void modifierQuantite_IllegalArgument_ThrowsBadRequestException() {
        // Arrange
        when(panierService.modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto)).thenThrow(new IllegalArgumentException("Quantité invalide"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.modifierQuantite(userId, modifierQuantiteDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Quantité invalide", exception.getReason());
        verify(panierService, times(1)).modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto);
    }

    @Test
    void modifierQuantite_IllegalState_ThrowsConflictException() {
        // Arrange
        when(panierService.modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto)).thenThrow(new IllegalStateException("Nombre de places insuffisant"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.modifierQuantite(userId, modifierQuantiteDto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Nombre de places insuffisant", exception.getReason());
        verify(panierService, times(1)).modifierQuantiteOffrePanier(userId.toString(), modifierQuantiteDto);
    }

    @Test
    void supprimerOffre_ExistingOffre_ReturnsOkPanierDto() {
        // Arrange
        when(panierService.supprimerOffreDuPanier(userId.toString(), offreId)).thenReturn(panierDto);

        // Act
        ResponseEntity<PanierDto> response = panierController.supprimerOffre(userId, offreId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(panierDto, response.getBody());
        verify(panierService, times(1)).supprimerOffreDuPanier(userId.toString(), offreId);
    }

    @Test
    void supprimerOffre_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(panierService.supprimerOffreDuPanier(userId.toString(), offreId)).thenThrow(new ResourceNotFoundException("Offre non trouvée dans le panier"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.supprimerOffre(userId, offreId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Offre non trouvée dans le panier", exception.getReason());
        verify(panierService, times(1)).supprimerOffreDuPanier(userId.toString(), offreId);
    }

    @Test
    void payerPanier_ValidPanier_ReturnsOkPanierDto() {
        // Arrange
        when(panierService.finaliserAchat(userId.toString())).thenReturn(panierDto);

        // Act
        ResponseEntity<PanierDto> response = panierController.payerPanier(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(panierDto, response.getBody());
        verify(panierService, times(1)).finaliserAchat(userId.toString());
    }

    @Test
    void payerPanier_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(panierService.finaliserAchat(userId.toString())).thenThrow(new ResourceNotFoundException("Panier non trouvé pour cet utilisateur"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.payerPanier(userId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Panier non trouvé pour cet utilisateur", exception.getReason());
        verify(panierService, times(1)).finaliserAchat(userId.toString());
    }

    @Test
    void payerPanier_IllegalState_ThrowsConflictException() {
        // Arrange
        when(panierService.finaliserAchat(userId.toString())).thenThrow(new IllegalStateException("Le panier ne peut pas être payé"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> panierController.payerPanier(userId));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Le panier ne peut pas être payé", exception.getReason());
        verify(panierService, times(1)).finaliserAchat(userId.toString());
    }
}
