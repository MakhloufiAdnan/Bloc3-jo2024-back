package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.paiement.PaiementDto;
import fr.studi.bloc3jo2024.dto.paiement.PaiementSimulationResultDto;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.service.PaiementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementControllerTest {

    @InjectMocks
    private PaiementController paiementController;

    @Mock
    private PaiementService paiementService;

    private final UUID utilisateurId = UUID.randomUUID();
    private final Long panierId = 1L;
    private final Long paiementId = 2L;
    private final MethodePaiementEnum methodePaiement = MethodePaiementEnum.CARTE_BANCAIRE;
    private final PaiementDto paiementDto = new PaiementDto();
    private final PaiementSimulationResultDto simulationResultDto = new PaiementSimulationResultDto();

    private void mockAuthentication() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(utilisateurId.toString());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void mockInvalidAuthentication() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("invalid-uuid");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void effectuerPaiement_ValidInput_ReturnsCreatedPaiementDto() {
        // Arrange
        mockAuthentication();
        when(paiementService.effectuerPaiement(utilisateurId, panierId, methodePaiement)).thenReturn(paiementDto);

        // Act
        ResponseEntity<PaiementDto> response = paiementController.effectuerPaiement(panierId, methodePaiement);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(paiementDto, response.getBody());
        verify(paiementService, times(1)).effectuerPaiement(utilisateurId, panierId, methodePaiement);
        clearAuthentication();
    }

    @Test
    void effectuerPaiement_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        mockAuthentication();
        when(paiementService.effectuerPaiement(utilisateurId, panierId, methodePaiement))
                .thenThrow(new ResourceNotFoundException("Panier non trouvé"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.effectuerPaiement(panierId, methodePaiement));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Panier non trouvé", exception.getReason());
        verify(paiementService, times(1)).effectuerPaiement(utilisateurId, panierId, methodePaiement);
        clearAuthentication();
    }

    @Test
    void effectuerPaiement_IllegalState_ThrowsConflictException() {
        // Arrange
        mockAuthentication();
        when(paiementService.effectuerPaiement(utilisateurId, panierId, methodePaiement))
                .thenThrow(new IllegalStateException("Le paiement a déjà été effectué pour ce panier"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.effectuerPaiement(panierId, methodePaiement));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Le paiement a déjà été effectué pour ce panier", exception.getReason());
        verify(paiementService, times(1)).effectuerPaiement(utilisateurId, panierId, methodePaiement);
        clearAuthentication();
    }

    @Test
    void effectuerPaiement_IllegalArgument_ThrowsBadRequestException() {
        // Arrange
        mockAuthentication();
        when(paiementService.effectuerPaiement(utilisateurId, panierId, methodePaiement))
                .thenThrow(new IllegalArgumentException("Méthode de paiement invalide"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.effectuerPaiement(panierId, methodePaiement));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Méthode de paiement invalide", exception.getReason());
        verify(paiementService, times(1)).effectuerPaiement(utilisateurId, panierId, methodePaiement);
        clearAuthentication();
    }

    @Test
    void effectuerPaiement_InvalidAuthenticatedUserId_ThrowsInternalServerError() {
        // Arrange
        mockInvalidAuthentication();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> paiementController.effectuerPaiement(panierId, methodePaiement),
                "L'identifiant de l'utilisateur authentifié n'est pas un UUID valide.");
        verify(paiementService, never()).effectuerPaiement(any(), any(), any());
        clearAuthentication();
    }

    @Test
    void simulerResultatPaiement_ValidInput_ReturnsOkSimulationResultDto() {
        // Arrange
        final boolean reussi = true;
        final String details = "Paiement réussi";
        when(paiementService.simulerResultatPaiement(paiementId, reussi, details)).thenReturn(simulationResultDto);

        // Act
        ResponseEntity<PaiementSimulationResultDto> response = paiementController.simulerResultatPaiement(paiementId, reussi, details);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(simulationResultDto, response.getBody());
        verify(paiementService, times(1)).simulerResultatPaiement(paiementId, reussi, details);
    }

    @Test
    void simulerResultatPaiement_ResourceNotFound_ThrowsNotFoundException() {
        // Arrange
        final boolean reussi = false;
        final String details = "Paiement refusé";
        when(paiementService.simulerResultatPaiement(paiementId, reussi, details))
                .thenThrow(new ResourceNotFoundException("Paiement non trouvé"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.simulerResultatPaiement(paiementId, reussi, details));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Paiement non trouvé", exception.getReason());
        verify(paiementService, times(1)).simulerResultatPaiement(paiementId, reussi, details);
    }

    @Test
    void simulerResultatPaiement_ValidInputWithoutDetails_ReturnsOkSimulationResultDto() {
        // Arrange
        final boolean reussi = true;
        final String details = "";
        when(paiementService.simulerResultatPaiement(paiementId, reussi, details)).thenReturn(simulationResultDto);

        // Act
        ResponseEntity<PaiementSimulationResultDto> response = paiementController.simulerResultatPaiement(paiementId, reussi, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(simulationResultDto, response.getBody());
        verify(paiementService, times(1)).simulerResultatPaiement(paiementId, reussi, details);
    }

    @Test
    void getPaiementParPanier_ExistingPaiement_ReturnsOkPaiementDto() {
        // Arrange
        mockAuthentication();
        when(paiementService.getPaiementParPanier(utilisateurId, panierId)).thenReturn(Optional.of(paiementDto));

        // Act
        ResponseEntity<PaiementDto> response = paiementController.getPaiementParPanier(panierId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paiementDto, response.getBody());
        verify(paiementService, times(1)).getPaiementParPanier(utilisateurId, panierId);
        clearAuthentication();
    }

    @Test
    void getPaiementParPanier_NonExistingPaiement_ThrowsNotFoundException() {
        // Arrange
        mockAuthentication();
        when(paiementService.getPaiementParPanier(utilisateurId, panierId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.getPaiementParPanier(panierId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Paiement non trouvé pour ce panier et cet utilisateur", exception.getReason());
        verify(paiementService, times(1)).getPaiementParPanier(utilisateurId, panierId);
        clearAuthentication();
    }

    @Test
    void getPaiementParPanier_InvalidAuthenticatedUserId_ThrowsInternalServerError() {
        // Arrange
        mockInvalidAuthentication();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> paiementController.getPaiementParPanier(panierId),
                "Impossible de récupérer l'ID de l'utilisateur authentifié.");
        verify(paiementService, never()).getPaiementParPanier(any(), any());
        clearAuthentication();
    }

    @Test
    void getPaiementParId_ExistingPaiement_ReturnsOkPaiementDto() {
        // Arrange
        when(paiementService.getPaiementParId(paiementId)).thenReturn(Optional.of(paiementDto));

        // Act
        ResponseEntity<PaiementDto> response = paiementController.getPaiementParId(paiementId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paiementDto, response.getBody());
        verify(paiementService, times(1)).getPaiementParId(paiementId);
    }

    @Test
    void getPaiementParId_NonExistingPaiement_ThrowsNotFoundException() {
        // Arrange
        when(paiementService.getPaiementParId(paiementId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paiementController.getPaiementParId(paiementId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Paiement non trouvé avec l'ID : " + paiementId, exception.getReason());
        verify(paiementService, times(1)).getPaiementParId(paiementId);
    }
}