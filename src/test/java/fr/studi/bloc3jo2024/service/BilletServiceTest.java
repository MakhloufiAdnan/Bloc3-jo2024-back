package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.repository.BilletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Keep ReflectionTestUtils

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilletServiceTest {

    @Mock
    private BilletRepository billetRepository;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BilletService billetService;

    // Made final and initialized directly
    private final String sujetEmailBillet = "Votre Billet";
    private final String contenuEmailBillet = "Bonjour %s, voici votre billet avec la clé : %s";

    // Use UUID for user ID
    private UUID userId;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to set the @Value fields on the billetService instance
        ReflectionTestUtils.setField(billetService, "sujetEmailBillet", sujetEmailBillet);
        ReflectionTestUtils.setField(billetService, "contenuEmailBillet", contenuEmailBillet);


        // Initialize UUID and Utilisateur
        userId = UUID.randomUUID();
        utilisateur = Utilisateur.builder()
                .idUtilisateur(userId) // Use UUID
                .cleUtilisateur("userKey")
                .email("test@example.com")
                .prenom("Jean")
                .build();
    }

    @Test
    void genererCleAchat_shouldReturnUUIDString() {
        // Arrange - Nothing specific to arrange

        // Act
        String cleAchat = billetService.genererCleAchat();

        // Assert
        assertNotNull(cleAchat);
        // Basic check if it looks like a UUID (hyphens)
        assertTrue(cleAchat.contains("-"));
        // Can add more robust UUID format validation if needed
    }

    @Test
    void genererCleFinaleBillet_shouldConcatenateKeys() {
        // Arrange
        String cleUtilisateur = "user123";
        String cleAchat = "achat456";
        String expectedCleFinale = "user123-achat456";

        // Act
        String cleFinale = billetService.genererCleFinaleBillet(cleUtilisateur, cleAchat);

        // Assert
        assertEquals(expectedCleFinale, cleFinale);
    }

    @Test
    void creerEtEnregistrerBillet_shouldSaveBillet() {
        // Arrange
        Offre offre1 = Offre.builder().idOffre(1L).typeOffre(TypeOffre.SOLO).build();
        Offre offre2 = Offre.builder().idOffre(2L).typeOffre(TypeOffre.DUO).build();
        List<Offre> offres = Arrays.asList(offre1, offre2);
        String cleFinaleBillet = "finalKey";

        Billet expectedBillet = Billet.builder()
                .utilisateur(utilisateur)
                .offres(offres)
                .cleFinaleBillet(cleFinaleBillet)
                .build();

        // Use any(Billet.class) as we don't need to match the exact instance created internally
        when(billetRepository.save(any(Billet.class))).thenReturn(expectedBillet);

        // Act
        Billet createdBillet = billetService.creerEtEnregistrerBillet(utilisateur, offres, cleFinaleBillet);

        // Assert
        assertNotNull(createdBillet);
        assertEquals(utilisateur, createdBillet.getUtilisateur());
        assertEquals(offres, createdBillet.getOffres());
        assertEquals(cleFinaleBillet, createdBillet.getCleFinaleBillet());
        // Verify save was called with any Billet instance
        verify(billetRepository, times(1)).save(any(Billet.class));
    }

    @Test
    void finaliserBilletAvecQrCode_shouldGenerateQrCodeSaveAndSendEmail() {
        // Arrange
        Billet initialBillet = Billet.builder()
                .idBillet(1L) // idBillet is still Long
                .cleFinaleBillet("finalKey")
                .utilisateur(utilisateur) // Use the initialized user
                .build();
        byte[] qrCodeImage = "dummy_qr_code".getBytes();

        // Mock QR code generation
        when(qrCodeService.generateQRCode(initialBillet.getCleFinaleBillet())).thenReturn(qrCodeImage);

        // Mock saving the billet (returning the same instance, potentially updated)
        when(billetRepository.save(initialBillet)).thenReturn(initialBillet);

        // Act
        Billet finalBillet = billetService.finaliserBilletAvecQrCode(initialBillet);

        // Assert
        assertNotNull(finalBillet);
        assertArrayEquals(qrCodeImage, finalBillet.getQrCodeImage());

        // Verify QR code was generated with the correct key
        verify(qrCodeService, times(1)).generateQRCode(initialBillet.getCleFinaleBillet());

        // Verify billet was saved
        verify(billetRepository, times(1)).save(initialBillet);

        // Verify email was sent with correct details
        verify(emailService, times(1)).envoyerEmailAvecQrCode(
                utilisateur.getEmail(), // Direct value
                sujetEmailBillet, // Direct value (final field)
                String.format(contenuEmailBillet, utilisateur.getPrenom(), initialBillet.getCleFinaleBillet()), // Direct value
                qrCodeImage, // Direct value
                "billet_" + initialBillet.getCleFinaleBillet() + ".png" // Direct value
        );
    }

    @Test
    void recupererBilletParId_shouldReturnOptionalBilletWhenFound() {
        // Arrange
        Long billetId = 1L; // idBillet is still Long
        Billet expectedBillet = Billet.builder().idBillet(billetId).build();
        when(billetRepository.findById(billetId)).thenReturn(Optional.of(expectedBillet));

        // Act
        Optional<Billet> result = billetService.recupererBilletParId(billetId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedBillet, result.get());
        // Verify findById was called with the correct ID (direct value)
        verify(billetRepository, times(1)).findById(billetId);
    }

    @Test
    void recupererBilletParId_shouldReturnEmptyOptionalWhenNotFound() {
        // Arrange
        Long billetId = 1L; // idBillet is still Long
        when(billetRepository.findById(billetId)).thenReturn(Optional.empty());

        // Act
        Optional<Billet> result = billetService.recupererBilletParId(billetId);

        // Assert
        assertFalse(result.isPresent());
        // Verify findById was called with the correct ID (direct value)
        verify(billetRepository, times(1)).findById(billetId);
    }

    @Test
    void recupererBilletParCleFinale_shouldReturnBilletWhenFound() {
        // Arrange
        String cleFinale = "finalKey";
        Billet expectedBillet = Billet.builder().cleFinaleBillet(cleFinale).build();
        // Mock repository call
        when(billetRepository.findByCleFinaleBillet(cleFinale)).thenReturn(Optional.of(expectedBillet));

        // Act
        Billet result = billetService.recupererBilletParCleFinale(cleFinale);

        // Assert
        assertNotNull(result);
        assertEquals(expectedBillet, result);
        // Verify findByCleFinaleBillet was called with the correct key (direct value)
        verify(billetRepository, times(1)).findByCleFinaleBillet(cleFinale);
    }

    @Test
    void recupererBilletParCleFinale_shouldThrowExceptionWhenNotFound() {
        // Arrange
        String cleFinale = "finalKey";
        // Mock repository call to return empty
        when(billetRepository.findByCleFinaleBillet(cleFinale)).thenReturn(Optional.empty());

        // Act & Assert
        // Converted to expression lambda
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> billetService.recupererBilletParCleFinale(cleFinale));

        assertTrue(thrown.getMessage().contains("Billet non trouvé avec la clé : " + cleFinale));
        // Verify findByCleFinaleBillet was called with the correct key (direct value)
        verify(billetRepository, times(1)).findByCleFinaleBillet(cleFinale);
    }
}
