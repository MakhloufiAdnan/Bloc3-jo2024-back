package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.exception.BilletAlreadyScannedException;
import fr.studi.bloc3jo2024.exception.BilletNotFoundException;
import fr.studi.bloc3jo2024.repository.BilletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test unitaire pour {@link BilletService}.
 * Utilise Mockito pour simuler les dépendances et tester la logique du service.
 */
@ExtendWith(MockitoExtension.class)
class BilletServiceTest {

    @Mock
    private BilletRepository billetRepository; // Mock du repository
    @Mock
    private QrCodeService qrCodeService; // Mock du service de QR code
    @Mock
    private EmailService emailService; // Mock du service d'email
    @Mock
    private BilletQueryService billetQueryService; // Mock du nouveau service de lecture

    @InjectMocks
    private BilletService billetService; // Le service à tester, avec ses mocks injectés

    private Utilisateur utilisateurExemple;
    private Billet billetExemple;
    private Offre offreExemple;
    private final LocalDateTime dummyPurchaseDate = LocalDateTime.of(2024, 7, 15, 10, 30);

    /**
     * Configuration initiale avant chaque test.
     * Initialise les objets de données de base et les valeurs des propriétés.
     */
    @BeforeEach
    void setUp() {
        // Initialisation des propriétés @Value via ReflectionTestUtils pour les tests unitaires
        ReflectionTestUtils.setField(billetService, "sujetEmailBillet", "Votre billet pour les JO");
        ReflectionTestUtils.setField(billetService, "contenuEmailBillet", "Bonjour %s, voici votre clé: %s.");

        utilisateurExemple = Utilisateur.builder()
                .idUtilisateur(UUID.randomUUID())
                .prenom("Alice")
                .nom("Dupont")
                .email("alice.dupont@example.com")
                .build();

        offreExemple = Offre.builder()
                .typeOffre(TypeOffre.SOLO)
                .build();

        // Initialisation de billetExemple avec des valeurs par défaut pour scannedAt et isScanned
        billetExemple = Billet.builder()
                .idBillet(1L)
                .cleFinaleBillet("CLE-TEST-123")
                .utilisateur(utilisateurExemple)
                .offres(Collections.singletonList(offreExemple))
                .purchaseDate(dummyPurchaseDate)
                .isScanned(false) // Initialisé à false par défaut pour les tests
                .scannedAt(null) // Peut être null initialement si non scanné
                .build();
    }

    /**
     * Teste la génération d'une clé d'achat.
     * Vérifie que la clé générée est un UUID valide.
     */
    @Test
    void genererCleAchat_ReturnsValidUUID() {
        String cleAchat = billetService.genererCleAchat();
        assertNotNull(cleAchat);
        // Tente de parser pour s'assurer que c'est un UUID valide
        assertDoesNotThrow(() -> UUID.fromString(cleAchat));
    }

    /**
     * Teste la génération de la clé finale d'un billet.
     * Vérifie que la clé est correctement concaténée.
     */
    @Test
    void genererCleFinaleBillet_CombinesKeysCorrectly() {
        String cleUtilisateur = "USER-ABC";
        String cleAchat = "XYZ-123";
        String expectedCleFinale = "USER-ABC-XYZ-123";
        String actualCleFinale = billetService.genererCleFinaleBillet(cleUtilisateur, cleAchat);
        assertEquals(expectedCleFinale, actualCleFinale);
    }

    /**
     * Teste la création et l'enregistrement d'un billet.
     * Vérifie que le billet est sauvegardé et que les propriétés sont correctement définies.
     */
    @Test
    void creerEtEnregistrerBillet_SavesBilletCorrectly() {
        // Arrange
        String cleFinaleArgument = "FINAL-KEY-ABC"; // La clé finale passée en argument
        // Utilisation d'ArgumentCaptor pour capturer l'objet Billet passé à save()
        ArgumentCaptor<Billet> billetCaptor = ArgumentCaptor.forClass(Billet.class);
        when(billetRepository.save(billetCaptor.capture())).thenReturn(billetExemple); // Mock le retour, mais capture l'argument

        // Act
        Billet savedBillet = billetService.creerEtEnregistrerBillet(utilisateurExemple, Collections.singletonList(offreExemple), cleFinaleArgument, dummyPurchaseDate);

        // Assert
        // Récupérer le billet qui a été effectivement passé à la méthode save
        Billet capturedBillet = billetCaptor.getValue();

        assertNotNull(savedBillet);
        // Les assertions doivent porter sur le billet capturé pour vérifier ce qui a été envoyé à la couche de persistance
        assertEquals(utilisateurExemple, capturedBillet.getUtilisateur());
        assertEquals(Collections.singletonList(offreExemple), capturedBillet.getOffres());
        assertEquals(cleFinaleArgument, capturedBillet.getCleFinaleBillet()); // Vérifie que la clé passée est celle utilisée
        assertEquals(dummyPurchaseDate, capturedBillet.getPurchaseDate());

        verify(billetRepository, times(1)).save(any(Billet.class));
    }

    /**
     * Teste la finalisation d'un billet avec génération de QR code et envoi d'email.
     * Vérifie que les services de QR code et d'email sont appelés correctement.
     */
    @Test
    void finaliserBilletAvecQrCode_GeneratesQrAndSendsEmail() {
        // Arrange
        byte[] qrCodeBytes = "qrcode_data".getBytes();
        when(qrCodeService.generateQRCode(anyString())).thenReturn(qrCodeBytes);
        when(billetRepository.save(any(Billet.class))).thenReturn(billetExemple); // Mock pour le save du billet

        // Act
        Billet finalBillet = billetService.finaliserBilletAvecQrCode(billetExemple);

        // Assert
        assertNotNull(finalBillet);
        assertArrayEquals(qrCodeBytes, finalBillet.getQrCodeImage());
        verify(qrCodeService, times(1)).generateQRCode(billetExemple.getCleFinaleBillet());
        verify(billetRepository, times(1)).save(billetExemple);

        // Capture d'arguments pour l'emailService
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> qrCodeImageCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).envoyerEmailAvecQrCode(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture(),
                qrCodeImageCaptor.capture(),
                fileNameCaptor.capture()
        );

        assertEquals(utilisateurExemple.getEmail(), emailCaptor.getValue());
        assertEquals("Votre billet pour les JO", subjectCaptor.getValue());
        assertTrue(contentCaptor.getValue().contains(utilisateurExemple.getPrenom()));
        assertTrue(contentCaptor.getValue().contains(billetExemple.getCleFinaleBillet()));
        assertArrayEquals(qrCodeBytes, qrCodeImageCaptor.getValue());
        assertTrue(fileNameCaptor.getValue().startsWith("billet_"));
        assertTrue(fileNameCaptor.getValue().endsWith(".png"));
    }

    /**
     * Teste la récupération d'un billet par ID.
     * Vérifie que le billet est retourné si trouvé.
     */
    @Test
    void recupererBilletParId_BilletFound() {
        // Arrange
        Long billetId = 1L;
        // La méthode de lecture est maintenant dans BilletQueryService
        when(billetQueryService.recupererBilletParId(billetId)).thenReturn(Optional.of(billetExemple));

        // Act
        Optional<Billet> foundBillet = billetQueryService.recupererBilletParId(billetId); // Appel direct sur BilletQueryService

        // Assert
        assertTrue(foundBillet.isPresent());
        assertEquals(billetExemple, foundBillet.get());
        verify(billetQueryService, times(1)).recupererBilletParId(billetId);
    }

    /**
     * Teste la récupération d'un billet par ID lorsque le billet n'existe pas.
     * Vérifie qu'un Optional.empty est retourné.
     */
    @Test
    void recupererBilletParId_BilletNotFound() {
        // Arrange
        Long billetId = 99L;
        // La méthode de lecture est maintenant dans BilletQueryService
        when(billetQueryService.recupererBilletParId(billetId)).thenReturn(Optional.empty());

        // Act
        Optional<Billet> foundBillet = billetQueryService.recupererBilletParId(billetId); // Appel direct sur BilletQueryService

        // Assert
        assertTrue(foundBillet.isEmpty());
        verify(billetQueryService, times(1)).recupererBilletParId(billetId);
    }

    /**
     * Teste la récupération d'un billet par clé finale lorsque le billet est trouvé.
     * Vérifie que le billet est retourné.
     */
    @Test
    void recupererBilletParCleFinale_BilletFound() {
        // Arrange
        String cleFinale = "CLE-TEST-123";
        // La méthode est maintenant dans BilletQueryService
        when(billetQueryService.recupererBilletParCleFinale(cleFinale)).thenReturn(billetExemple);

        // Act
        Billet foundBillet = billetQueryService.recupererBilletParCleFinale(cleFinale); // Appel direct sur BilletQueryService

        // Assert
        assertNotNull(foundBillet);
        assertEquals(billetExemple, foundBillet);
        verify(billetQueryService, times(1)).recupererBilletParCleFinale(cleFinale);
    }

    /**
     * Teste la récupération d'un billet par clé finale lorsque le billet n'existe pas.
     * Vérifie qu'une BilletNotFoundException est levée.
     */
    @Test
    void recupererBilletParCleFinale_BilletNotFound() {
        // Arrange
        String cleFinale = "CLE-INCONNUE";
        // La méthode est maintenant dans BilletQueryService
        when(billetQueryService.recupererBilletParCleFinale(cleFinale)).thenThrow(new BilletNotFoundException("Billet non trouvé"));

        // Act & Assert
        BilletNotFoundException thrown = assertThrows(BilletNotFoundException.class, () ->
                billetQueryService.recupererBilletParCleFinale(cleFinale) // Appel direct sur BilletQueryService
        );
        assertTrue(thrown.getMessage().contains("Billet non trouvé"));
        verify(billetQueryService, times(1)).recupererBilletParCleFinale(cleFinale);
    }

    /**
     * Teste la vérification et le marquage d'un billet comme scanné (succès).
     * Vérifie que le billet est mis à jour et sauvegardé.
     */
    @Test
    void verifierEtMarquerCommeScanne_Success() {
        // Arrange
        String cleFinale = "CLE-TEST-123";
        // BilletQueryService récupère le billet non scanné
        when(billetQueryService.recupererBilletParCleFinale(cleFinale)).thenReturn(billetExemple);
        // Le save retourne le billet mis à jour (mocké pour simplifier)
        when(billetRepository.save(any(Billet.class))).thenReturn(billetExemple);

        // Act
        Billet updatedBillet = billetService.verifierEtMarquerCommeScanne(cleFinale);

        // Assert
        assertNotNull(updatedBillet);
        assertTrue(updatedBillet.isScanned());
        assertNotNull(updatedBillet.getScannedAt());
        verify(billetQueryService, times(1)).recupererBilletParCleFinale(cleFinale);
        verify(billetRepository, times(1)).save(billetExemple);
    }

    /**
     * Teste le scénario où un billet déjà scanné est vérifié.
     * Vérifie qu'une BilletAlreadyScannedException est levée.
     */
    @Test
    void verifierEtMarquerCommeScanne_AlreadyScanned() {
        // Arrange
        String cleFinale = "CLE-DEJA-SCANNEE";
        // Crée un billet qui est déjà scanné
        Billet alreadyScannedBillet = Billet.builder()
                .idBillet(2L)
                .cleFinaleBillet(cleFinale)
                .utilisateur(utilisateurExemple)
                .offres(Collections.singletonList(offreExemple))
                .purchaseDate(dummyPurchaseDate)
                .isScanned(true) // Marqué comme scanné
                .scannedAt(LocalDateTime.now().minusHours(1)) // Date de scan passée
                .build();

        // BilletQueryService récupère le billet déjà scanné
        when(billetQueryService.recupererBilletParCleFinale(cleFinale)).thenReturn(alreadyScannedBillet);

        // Act & Assert
        BilletAlreadyScannedException thrown = assertThrows(BilletAlreadyScannedException.class, () ->
                billetService.verifierEtMarquerCommeScanne(cleFinale)
        );
        // Vérifie que le message contient la partie fixe attendue
        assertTrue(thrown.getMessage().contains("Ce billet a déjà été scanné le "));
        verify(billetQueryService, times(1)).recupererBilletParCleFinale(cleFinale);
        verify(billetRepository, never()).save(any(Billet.class)); // S'assurer que save n'est pas appelé
    }

    /**
     * Teste la récupération des clés de billets valides.
     * Vérifie que la liste des clés non scannées est retournée.
     */
    @Test
    void getClesBilletsValides_ReturnsUnscannedKeys() {
        // Arrange
        List<String> expectedKeys = Arrays.asList("KEY1", "KEY2");
        // La méthode est maintenant dans BilletQueryService
        when(billetQueryService.getClesBilletsValides()).thenReturn(expectedKeys);

        // Act
        List<String> actualKeys = billetQueryService.getClesBilletsValides(); // Appel direct sur BilletQueryService

        // Assert
        assertNotNull(actualKeys);
        assertEquals(expectedKeys.size(), actualKeys.size());
        assertEquals(expectedKeys, actualKeys);
        verify(billetQueryService, times(1)).getClesBilletsValides();
    }
}