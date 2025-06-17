package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.billets.BilletVerificationDto;
import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.BilletAlreadyScannedException;
import fr.studi.bloc3jo2024.exception.BilletNotFoundException;
import fr.studi.bloc3jo2024.service.BilletService;
import fr.studi.bloc3jo2024.service.BilletQueryService; // Importation du service de lecture
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test unitaire pour {@link BilletController}.
 * Utilise Mockito pour simuler les dépendances (services) et tester la logique du contrôleur
 * de manière isolée.
 */
@ExtendWith(MockitoExtension.class)
class BilletControllerTest {

    @Mock
    private BilletService billetService; // Mock du service pour les opérations de modification/transactionnelles

    @Mock
    private BilletQueryService billetQueryService; // Mock du service pour les opérations de lecture

    @InjectMocks
    private BilletController billetController; // Le contrôleur à tester, avec ses mocks injectés

    private Billet billetExemple;
    private Utilisateur utilisateurExemple;
    private byte[] qrCodeExemple;
    private final LocalDateTime dummyPurchaseDate = LocalDateTime.of(2024, 7, 15, 10, 30); // Date d'achat factice

    /**
     * Méthode de configuration exécutée avant chaque test.
     * Initialise les objets de test (utilisateur, offres, billet) nécessaires pour les scénarios.
     */
    @BeforeEach
    void setUp() {
        utilisateurExemple = Utilisateur.builder()
                .idUtilisateur(UUID.randomUUID())
                .prenom("Jean")
                .nom("Dupont")
                .build();

        Offre offreSoloExemple = Offre.builder().typeOffre(TypeOffre.SOLO).build();
        Offre offreDuoExemple = Offre.builder().typeOffre(TypeOffre.DUO).build();

        qrCodeExemple = new byte[]{1, 2, 3, 4}; // Représentation simple d'un QR code en bytes

        billetExemple = Billet.builder()
                .idBillet(1L)
                .cleFinaleBillet("CLE12345")
                .utilisateur(utilisateurExemple)
                .offres(Arrays.asList(offreSoloExemple, offreDuoExemple)) // Utilisation de Arrays.asList pour plusieurs éléments
                .qrCodeImage(qrCodeExemple)
                .purchaseDate(dummyPurchaseDate)
                .build();
    }

    // --- Tests pour l'endpoint GET /api/billets/{id}/qr-code (getQRCodeForBillet) ---

    /**
     * Teste le scénario où la récupération du QR code d'un billet réussit.
     * Vérifie le statut HTTP (200 OK), le corps de la réponse et le type de média.
     */
    @Test
    void getQRCodeForBillet_Success() {
        // Arrange : Préparation des données et du comportement du mock
        Long billetId = 1L;
        // Le contrôleur appelle maintenant billetQueryService pour cette opération de lecture
        when(billetQueryService.recupererBilletParId(billetId)).thenReturn(Optional.of(billetExemple));

        // Act : Appel de la méthode à tester du contrôleur
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert : Vérification des résultats
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(qrCodeExemple, response.getBody()); // Le QR code retourné doit correspondre à celui mocké
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        // Vérification que la méthode appropriée du service de lecture a été appelée une fois
        verify(billetQueryService, times(1)).recupererBilletParId(billetId);
    }

    /**
     * Teste le scénario où le billet demandé n'est pas trouvé.
     * Vérifie le statut HTTP (404 NOT_FOUND).
     */
    @Test
    void getQRCodeForBillet_BilletNotFound() {
        // Arrange
        Long billetId = 99L;
        // Le contrôleur appelle billetQueryService qui renvoie un Optional vide
        when(billetQueryService.recupererBilletParId(billetId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody()); // Le corps de la réponse doit être nul pour un 404
        verify(billetQueryService, times(1)).recupererBilletParId(billetId);
    }

    /**
     * Teste le scénario où le billet est trouvé, mais ne contient pas de QR code.
     * Vérifie le statut HTTP (404 NOT_FOUND).
     */
    @Test
    void getQRCodeForBillet_QRCodeNotFoundOnBillet() {
        // Arrange
        Long billetId = 1L;
        Billet billetSansQrCode = Billet.builder()
                .idBillet(billetId)
                .cleFinaleBillet("CLE_SANS_QR")
                .utilisateur(utilisateurExemple)
                .offres(List.of(new Offre())) // Utilisation de List.of() pour un seul élément
                .purchaseDate(dummyPurchaseDate)
                .qrCodeImage(null) // QR Code explicitement manquant
                .build();
        // Le contrôleur appelle billetQueryService, qui retourne un billet sans QR code
        when(billetQueryService.recupererBilletParId(billetId)).thenReturn(Optional.of(billetSansQrCode));

        // Act
        ResponseEntity<byte[]> response = billetController.getQRCodeForBillet(billetId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(billetQueryService, times(1)).recupererBilletParId(billetId);
    }

    // --- Tests pour l'endpoint GET /api/billets/verifier/{cleFinaleBillet} (verifierBillet) ---

    /**
     * Teste le scénario de vérification réussie d'un billet.
     * Vérifie le statut HTTP (200 OK) et le contenu du DTO de réponse.
     */
    @Test
    void verifierBillet_Success() {
        // Arrange
        String cleFinale = "CLE12345";
        // La méthode verifierEtMarquerCommeScanne est toujours dans BilletService
        when(billetService.verifierEtMarquerCommeScanne(cleFinale)).thenReturn(billetExemple);

        // Act
        ResponseEntity<BilletVerificationDto> response = billetController.verifierBillet(cleFinale);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BilletVerificationDto dto = response.getBody();
        assertEquals(billetExemple.getIdBillet(), dto.getIdBillet());
        assertEquals(billetExemple.getCleFinaleBillet(), dto.getCleFinaleBillet());

        // Vérification des champs de l'utilisateur mappés
        assertNotNull(dto.getIdUtilisateur());
        assertEquals(utilisateurExemple.getIdUtilisateur(), dto.getIdUtilisateur());
        assertEquals("Jean Dupont", dto.getNomUtilisateur());

        // Vérification des types d'offre mappés
        assertNotNull(dto.getOffres());
        assertEquals(2, dto.getOffres().size());
        assertTrue(dto.getOffres().contains(TypeOffre.SOLO.name()));
        assertTrue(dto.getOffres().contains(TypeOffre.DUO.name()));

        // Vérification de la date d'achat
        assertEquals(dummyPurchaseDate, dto.getDateAchat());

        verify(billetService, times(1)).verifierEtMarquerCommeScanne(cleFinale);
    }

    /**
     * Teste le scénario de vérification réussie d'un billet sans utilisateur ni offres associées.
     * Vérifie la gestion des valeurs nulles/vides pour ces champs.
     */
    @Test
    void verifierBillet_Success_WithoutUserAndOffers() {
        // Arrange
        String cleFinale = "CLE67890";
        Billet billetSansUserNiOffre = Billet.builder()
                .idBillet(2L)
                .cleFinaleBillet(cleFinale)
                .utilisateur(null) // Pas d'utilisateur
                .offres(null) // Pas d'offres
                .purchaseDate(dummyPurchaseDate.minusDays(1)) // Initialisation de la purchaseDate
                .build();
        // Cette méthode est toujours dans BilletService
        when(billetService.verifierEtMarquerCommeScanne(cleFinale)).thenReturn(billetSansUserNiOffre);

        // Act
        ResponseEntity<BilletVerificationDto> response = billetController.verifierBillet(cleFinale);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        BilletVerificationDto dto = response.getBody();
        assertEquals(billetSansUserNiOffre.getIdBillet(), dto.getIdBillet());
        assertEquals(billetSansUserNiOffre.getCleFinaleBillet(), dto.getCleFinaleBillet());

        // Vérifier que les champs de l'utilisateur sont nuls
        assertNull(dto.getIdUtilisateur());
        assertNull(dto.getNomUtilisateur());

        // Vérifier que la liste des offres est vide (non nulle)
        assertNotNull(dto.getOffres());
        assertTrue(dto.getOffres().isEmpty());

        // Vérifier la date d'achat
        assertEquals(dummyPurchaseDate.minusDays(1), dto.getDateAchat());


        verify(billetService, times(1)).verifierEtMarquerCommeScanne(cleFinale);
    }

    /**
     * Teste le scénario où le billet à vérifier n'est pas trouvé.
     * Vérifie que le contrôleur lance une ResponseStatusException avec un statut 404 NOT_FOUND.
     */
    @Test
    void verifierBillet_NotFound() {
        // Arrange
        String cleFinale = "CLEINCONNUE";
        // Le service lève une BilletNotFoundException
        when(billetService.verifierEtMarquerCommeScanne(cleFinale)).thenThrow(new BilletNotFoundException("Billet non trouvé"));

        // Act & Assert : Vérification de l'exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                billetController.verifierBillet(cleFinale)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Billet non trouvé"));

        verify(billetService, times(1)).verifierEtMarquerCommeScanne(cleFinale);
    }

    /**
     * Teste le scénario où le billet à vérifier a déjà été scanné.
     * Vérifie que le contrôleur lance une ResponseStatusException avec un statut 409 CONFLICT.
     */
    @Test
    void verifierBillet_AlreadyScanned() {
        // Arrange
        String cleFinale = "CLE_DEJA_SCANNEE";
        // Le message d'exception simulé doit correspondre à celui que le BilletService est censé générer.
        // Utilisons une partie fixe du message que le service génère.
        String expectedServiceMessagePart = "Ce billet a déjà été scanné le ";
        when(billetService.verifierEtMarquerCommeScanne(cleFinale))
                .thenThrow(new BilletAlreadyScannedException(expectedServiceMessagePart + LocalDateTime.now()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                billetController.verifierBillet(cleFinale)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        // L'assertion doit chercher une partie générique et stable du message de l'exception
        assertTrue(exception.getReason().contains(expectedServiceMessagePart)); // CHANGEMENT D'ASSERTION ICI

        verify(billetService, times(1)).verifierEtMarquerCommeScanne(cleFinale);
    }

    /**
     * Teste le scénario où une erreur interne (ex: erreur de base de données) se produit lors de la vérification.
     * Vérifie que le contrôleur lance une ResponseStatusException avec un statut 500 INTERNAL_SERVER_ERROR.
     */
    @Test
    void verifierBillet_InternalServerError() {
        // Arrange
        String cleFinale = "CLEERREUR";
        // Le service lève une RuntimeException générique
        when(billetService.verifierEtMarquerCommeScanne(cleFinale)).thenThrow(new RuntimeException("Erreur DB"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                billetController.verifierBillet(cleFinale)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Erreur interne lors de la vérification du billet."));

        verify(billetService, times(1)).verifierEtMarquerCommeScanne(cleFinale);
    }

    // --- Tests pour l'endpoint GET /api/billets/sync/valid-keys (getValidTicketKeys) ---

    /**
     * Teste le scénario de récupération réussie des clés de billets valides pour la synchronisation.
     * Vérifie le statut HTTP (200 OK) et la liste des clés retournées.
     */
    @Test
    void getValidTicketKeys_Success() {
        // Arrange
        List<String> validKeys = Arrays.asList("key1", "key2", "key3");
        // Le contrôleur appelle billetQueryService pour cette opération de lecture
        when(billetQueryService.getClesBilletsValides()).thenReturn(validKeys);

        // Act
        ResponseEntity<List<String>> response = billetController.getValidTicketKeys();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validKeys, response.getBody());
        // Vérifier que la méthode appropriée du service de lecture a été appelée une fois
        verify(billetQueryService, times(1)).getClesBilletsValides();
    }

    /**
     * Teste le scénario où une erreur interne se produit lors de la récupération des clés valides.
     * Vérifie que le contrôleur retourne un statut 500 INTERNAL_SERVER_ERROR.
     */
    @Test
    void getValidTicketKeys_InternalServerError() {
        // Arrange
        // Le contrôleur appelle billetQueryService qui lève une RuntimeException
        when(billetQueryService.getClesBilletsValides()).thenThrow(new RuntimeException("DB error during sync"));

        // Act
        ResponseEntity<List<String>> response = billetController.getValidTicketKeys();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody()); // Le corps doit être nul en cas d'erreur 500 dans ce cas
        verify(billetQueryService, times(1)).getClesBilletsValides();
    }
}