package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.service.BilletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BilletCreationServiceImplTest {

    @Mock
    private BilletService billetService;

    @InjectMocks
    private BilletCreationServiceImpl billetCreationService;

    // These are real dummy objects, NOT mocks
    private Paiement dummyPaiement;
    private Utilisateur dummyUtilisateur;
    private Panier dummyPanier;
    private Offre dummyOffre1;
    private Offre dummyOffre2;
    private Billet dummyBilletInitial;
    private Billet dummyBilletFinal;

    private final String dummyCleAchat = "CLE_ACHAT_TEST";
    private final String dummyCleUtilisateur = "CLE_UTILISATEUR_TEST";
    private final String dummyCleFinaleBillet = "CLE_FINALE_TEST";
    private final UUID dummyUtilisateurId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Arrange common dummy data (real objects)
        dummyUtilisateur = new Utilisateur();
        dummyUtilisateur.setIdUtilisateur(dummyUtilisateurId);
        dummyUtilisateur.setCleUtilisateur(dummyCleUtilisateur);

        dummyOffre1 = new Offre();
        dummyOffre1.setIdOffre(10L);

        dummyOffre2 = new Offre();
        dummyOffre2.setIdOffre(20L);

        ContenuPanier dummyContenuPanier1;
        dummyContenuPanier1 = new ContenuPanier();
        dummyContenuPanier1.setOffre(dummyOffre1);

        ContenuPanier dummyContenuPanier2;
        dummyContenuPanier2 = new ContenuPanier();
        dummyContenuPanier2.setOffre(dummyOffre2);

        dummyPanier = new Panier();
        dummyPanier.setIdPanier(1000L);
        // Link ContenuPanier back to Panier and Offre relationships
        dummyContenuPanier1.setPanier(dummyPanier);
        dummyContenuPanier2.setPanier(dummyPanier);
        dummyPanier.setContenuPaniers(new HashSet<>(Arrays.asList(dummyContenuPanier1, dummyContenuPanier2)));

        dummyPaiement = new Paiement();
        dummyPaiement.setIdPaiement(10000L);
        dummyPaiement.setUtilisateur(dummyUtilisateur);
        dummyPaiement.setPanier(dummyPanier);

        dummyBilletInitial = new Billet();
        dummyBilletInitial.setIdBillet(1L);
        dummyBilletInitial.setCleFinaleBillet(dummyCleFinaleBillet);

        dummyBilletFinal = new Billet();
        dummyBilletFinal.setIdBillet(1L);
        dummyBilletFinal.setCleFinaleBillet(dummyCleFinaleBillet);
    }

    @Test
    void genererBilletApresTransactionReussie_Successful() {
        // Arrange
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);
        when(billetService.creerEtEnregistrerBillet(eq(dummyUtilisateur), anyList(), eq(dummyCleFinaleBillet))).thenReturn(dummyBilletInitial);
        when(billetService.finaliserBilletAvecQrCode(dummyBilletInitial)).thenReturn(dummyBilletFinal);

        // Act
        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        // Assert
        assertNotNull(resultBillet);
        assertEquals(dummyBilletFinal, resultBillet);

        // Verify interactions with the MOCKED BilletService
        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);
        // Verify the list passed to creerEtEnregistrerBillet contains the correct offers
        verify(billetService, times(1)).creerEtEnregistrerBillet(eq(dummyUtilisateur), argThat(offers -> offers.contains(dummyOffre1) && offers.contains(dummyOffre2) && offers.size() == 2), eq(dummyCleFinaleBillet));
        verify(billetService, times(1)).finaliserBilletAvecQrCode(dummyBilletInitial);

        // Removed verifications on real dummy objects like dummyPaiement, dummyPanier, etc.
    }

    @Test
    void genererBilletApresTransactionReussie_NullUtilisateur_ReturnsNull() {
        // Arrange
        dummyPaiement.setUtilisateur(null); // Set user to null

        // Act
        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        // Assert
        assertNull(resultBillet);

        // Verify interactions - No billet service methods should be called
        verifyNoInteractions(billetService);

        // Removed verifications on real dummy objects like dummyPaiement
    }

    @Test
    void genererBilletApresTransactionReussie_NullPanier_ReturnsNull() {
        // Arrange
        dummyPaiement.setPanier(null); // Set panier to null

        // Act
        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        // Assert
        assertNull(resultBillet);

        // Verify interactions - No billet service methods should be called
        verifyNoInteractions(billetService);

        // Removed verifications on real dummy objects like dummyPaiement
    }

    @Test
    void genererBilletApresTransactionReussie_EmptyOffersInPanier_ReturnsNull() {
        // Arrange
        dummyPanier.setContenuPaniers(new HashSet<>()); // Set an empty set of contenuPaniers
        dummyPaiement.setPanier(dummyPanier); // Ensure panier is linked

        // Key generation calls still happen before the empty check
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);


        // Act
        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        // Assert
        assertNull(resultBillet);

        // Verify interactions with the MOCKED BilletService
        // Key generation methods are called
        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);

        // Billet creation and finalization methods are NOT called
        verify(billetService, times(0)).creerEtEnregistrerBillet(any(), anyList(), any());
        verify(billetService, times(0)).finaliserBilletAvecQrCode(any());

        // Removed verifications on real dummy objects like dummyPaiement, dummyPanier, etc.
    }

    @Test
    void genererBilletApresTransactionReussie_NullContenuPaniers_ReturnsNull() {
        // Arrange
        dummyPanier.setContenuPaniers(null); // Set contenuPaniers to null
        dummyPaiement.setPanier(dummyPanier); // Ensure panier is linked

        // Key generation calls still happen before the empty check
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);

        // Act
        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        // Assert
        assertNull(resultBillet);

        // Verify interactions with the MOCKED BilletService
        // Key generation methods are called
        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);

        // Billet creation and finalization methods are NOT called
        verify(billetService, times(0)).creerEtEnregistrerBillet(any(), anyList(), any());
        verify(billetService, times(0)).finaliserBilletAvecQrCode(any());

        // Removed verifications on real dummy objects like dummyPaiement, dummyPanier, etc.
    }
}