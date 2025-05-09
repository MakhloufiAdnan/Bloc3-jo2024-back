package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.billets.BilletVerificationDto;
import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.service.BilletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilletControllerTest {

    @Mock
    private BilletService billetService;

    @InjectMocks
    private BilletController billetController;

    private Billet billetExemple;
    private Utilisateur utilisateurExemple;
    private byte[] qrCodeExemple;

    @BeforeEach
    void setUp() {
        // Arrange

        utilisateurExemple = new Utilisateur();
        utilisateurExemple.setIdUtilisateur(UUID.randomUUID());
        utilisateurExemple.setPrenom("Jean");
        utilisateurExemple.setNom("Dupont");

        Offre offreSoloExemple;
        offreSoloExemple = new Offre();
        offreSoloExemple.setTypeOffre(TypeOffre.SOLO);

        Offre offreDuoExemple;
        offreDuoExemple = new Offre();
        offreDuoExemple.setTypeOffre(TypeOffre.DUO);

        qrCodeExemple = new byte[]{1, 2, 3, 4}; // Exemple de données d'image QR Code

        billetExemple = new Billet();
        billetExemple.setIdBillet(1L);
        billetExemple.setCleFinaleBillet("CLE12345");
        billetExemple.setUtilisateur(utilisateurExemple);
        billetExemple.setOffres(Arrays.asList(offreSoloExemple, offreDuoExemple));
        billetExemple.setQrCodeImage(qrCodeExemple);
    }

    // --- Tests Endpoint GET /api/billets/{id}/qr-code (getQRCodeForBillet) ---

    @Test
    void getQRCodeForBillet_Success() {
        // Arrange
        Long billetId = 1L;
        when(billetService.recupererBilletParId(billetId)).thenReturn(Optional.of(billetExemple));

        // Act
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(qrCodeExemple, response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        verify(billetService, times(1)).recupererBilletParId(billetId);
    }

    @Test
    void getQRCodeForBillet_BilletNotFound() {
        // Arrange
        Long billetId = 99L;
        when(billetService.recupererBilletParId(billetId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Le corps doit être nul pour un 404 build()
        verify(billetService, times(1)).recupererBilletParId(billetId);
    }

    @Test
    void getQRCodeForBillet_QRCodeNotFoundOnBillet() {
        // Arrange
        Long billetId = 1L;
        Billet billetSansQrCode = new Billet();
        billetSansQrCode.setIdBillet(billetId);
        billetSansQrCode.setQrCodeImage(null); // QR Code manquant
        when(billetService.recupererBilletParId(billetId)).thenReturn(Optional.of(billetSansQrCode));

        // Act
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());// Le corps doit être nul car le filtre Optional.filter() l'a écarté
        verify(billetService, times(1)).recupererBilletParId(billetId);
    }

    // --- Tests Endpoint GET /api/billets/verifier/{cleFinaleBillet} (verifierBillet) ---

    @Test
    void verifierBillet_Success() {
        // Arrange
        String cleFinale = "CLE12345";
        when(billetService.recupererBilletParCleFinale(cleFinale)).thenReturn(billetExemple);

        // Act
        ResponseEntity<BilletVerificationDto> response = billetController.verifierBillet(cleFinale);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BilletVerificationDto dto = response.getBody();
        assertEquals(billetExemple.getIdBillet(), dto.getIdBillet());
        assertEquals(billetExemple.getCleFinaleBillet(), dto.getCleFinaleBillet());

        // Vérifier les champs de l'utilisateur
        assertNotNull(dto.getIdUtilisateur());
        assertEquals(utilisateurExemple.getIdUtilisateur(), dto.getIdUtilisateur());
        assertEquals("Jean Dupont", dto.getNomUtilisateur());

        // Vérifier les types d'offre mappés
        assertNotNull(dto.getOffres());
        assertEquals(2, dto.getOffres().size());
        assertTrue(dto.getOffres().contains(TypeOffre.SOLO.name()));
        assertTrue(dto.getOffres().contains(TypeOffre.DUO.name()));

        verify(billetService, times(1)).recupererBilletParCleFinale(cleFinale);
    }

    @Test
    void verifierBillet_Success_WithoutUserAndOffers() {
        // Arrange
        String cleFinale = "CLE67890";
        Billet billetSansUserNiOffre = new Billet();
        billetSansUserNiOffre.setIdBillet(2L);
        billetSansUserNiOffre.setCleFinaleBillet(cleFinale);
        billetSansUserNiOffre.setUtilisateur(null); // Pas d'utilisateur
        billetSansUserNiOffre.setOffres(null); // Pas d'offres
        when(billetService.recupererBilletParCleFinale(cleFinale)).thenReturn(billetSansUserNiOffre);

        // Act
        ResponseEntity<BilletVerificationDto> response = billetController.verifierBillet(cleFinale);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BilletVerificationDto dto = response.getBody();
        assertEquals(billetSansUserNiOffre.getIdBillet(), dto.getIdBillet());
        assertEquals(billetSansUserNiOffre.getCleFinaleBillet(), dto.getCleFinaleBillet());

        // Vérifier les champs de l'utilisateur sont nuls
        assertNull(dto.getIdUtilisateur());
        assertNull(dto.getNomUtilisateur());

        // Vérifier que la liste des offres est vide
        assertNotNull(dto.getOffres());
        assertTrue(dto.getOffres().isEmpty());

        verify(billetService, times(1)).recupererBilletParCleFinale(cleFinale);
    }

    @Test
    void verifierBillet_NotFound() {
        // Arrange
        String cleFinale = "CLEINCONNUE";
        // Le service lance une IllegalArgumentException si non trouvé
        when(billetService.recupererBilletParCleFinale(cleFinale)).thenThrow(new IllegalArgumentException("Billet non trouvé"));

        // Act & Assert
        // On s'attend à ce que le contrôleur lance une ResponseStatusException
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            billetController.verifierBillet(cleFinale);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Billet non trouvé")); // Vérifier une partie du message

        verify(billetService, times(1)).recupererBilletParCleFinale(cleFinale);
    }

    @Test
    void verifierBillet_InternalServerError() {
        // Arrange
        String cleFinale = "CLEERREUR";
        // Le service lance une autre exception (simulant une erreur interne)
        when(billetService.recupererBilletParCleFinale(cleFinale)).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        // On s'attend à ce que le contrôleur lance une ResponseStatusException pour erreur interne
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            billetController.verifierBillet(cleFinale);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Erreur interne lors de la vérification du billet.")); // Vérifier le message générique

        verify(billetService, times(1)).recupererBilletParCleFinale(cleFinale);
    }
}