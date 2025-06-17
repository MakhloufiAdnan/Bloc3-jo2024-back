package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import fr.studi.bloc3jo2024.service.BilletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    private Paiement dummyPaiement;
    private Utilisateur dummyUtilisateur;
    private Panier dummyPanier;
    private Offre dummyOffre1;
    private Offre dummyOffre2;
    private Billet dummyBilletInitial;
    private Billet dummyBilletFinal;
    private Transaction dummyTransaction;

    private final String dummyCleAchat = "CLE_ACHAT_TEST";
    private final String dummyCleUtilisateur = "CLE_UTILISATEUR_TEST";
    private final String dummyCleFinaleBillet = "CLE_FINALE_TEST";
    private final UUID dummyUtilisateurId = UUID.randomUUID();
    private final LocalDateTime dummyDateValidation = LocalDateTime.of(2024, 7, 15, 10, 0);

    @BeforeEach
    void setUp() {
        dummyUtilisateur = Utilisateur.builder()
                .idUtilisateur(dummyUtilisateurId)
                .cleUtilisateur(dummyCleUtilisateur)
                .email("test@example.com")
                .prenom("Jean")
                .build();

        dummyOffre1 = Offre.builder().idOffre(10L).typeOffre(TypeOffre.SOLO).build();
        dummyOffre2 = Offre.builder().idOffre(20L).typeOffre(TypeOffre.DUO).build();

        ContenuPanier dummyContenuPanier1 = ContenuPanier.builder().offre(dummyOffre1).build();
        ContenuPanier dummyContenuPanier2 = ContenuPanier.builder().offre(dummyOffre2).build();

        dummyPanier = Panier.builder()
                .idPanier(1000L)
                .contenuPaniers(new HashSet<>(Arrays.asList(dummyContenuPanier1, dummyContenuPanier2)))
                .build();
        dummyContenuPanier1.setPanier(dummyPanier);
        dummyContenuPanier2.setPanier(dummyPanier);

        dummyTransaction = Transaction.builder()
                .idTransaction(1L)
                .dateValidation(dummyDateValidation)
                .statutTransaction(StatutTransaction.REUSSI)
                .build();

        dummyPaiement = Paiement.builder()
                .idPaiement(10000L)
                .utilisateur(dummyUtilisateur)
                .panier(dummyPanier)
                .transaction(dummyTransaction)
                .build();
        dummyTransaction.setPaiement(dummyPaiement);


        dummyBilletInitial = Billet.builder()
                .idBillet(1L)
                .cleFinaleBillet(dummyCleFinaleBillet)
                .utilisateur(dummyUtilisateur)
                .offres(Arrays.asList(dummyOffre1, dummyOffre2))
                .purchaseDate(dummyDateValidation)
                .build();

        dummyBilletFinal = Billet.builder()
                .idBillet(1L)
                .cleFinaleBillet(dummyCleFinaleBillet)
                .utilisateur(dummyUtilisateur)
                .offres(Arrays.asList(dummyOffre1, dummyOffre2))
                .purchaseDate(dummyDateValidation)
                .qrCodeImage("dummy_qr_code".getBytes())
                .build();
    }

    @Test
    void genererBilletApresTransactionReussie_Successful() {
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);
        when(billetService.creerEtEnregistrerBillet(eq(dummyUtilisateur), anyList(), eq(dummyCleFinaleBillet), eq(dummyDateValidation)))
                .thenReturn(dummyBilletInitial);
        when(billetService.finaliserBilletAvecQrCode(dummyBilletInitial)).thenReturn(dummyBilletFinal);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNotNull(resultBillet);
        assertEquals(dummyBilletFinal, resultBillet);

        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);
        verify(billetService, times(1)).creerEtEnregistrerBillet(
                eq(dummyUtilisateur),
                argThat(offers -> offers.contains(dummyOffre1) && offers.contains(dummyOffre2) && offers.size() == 2),
                eq(dummyCleFinaleBillet),
                eq(dummyDateValidation)
        );
        verify(billetService, times(1)).finaliserBilletAvecQrCode(dummyBilletInitial);
    }

    @Test
    void genererBilletApresTransactionReussie_NullUtilisateur_ReturnsNull() {
        dummyPaiement.setUtilisateur(null);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);
        verifyNoInteractions(billetService);
    }

    @Test
    void genererBilletApresTransactionReussie_NullPanier_ReturnsNull() {
        dummyPaiement.setPanier(null);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);
        verifyNoInteractions(billetService);
    }

    @Test
    void genererBilletApresTransactionReussie_NullTransaction_ReturnsNull() {
        dummyPaiement.setTransaction(null);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);
        verifyNoInteractions(billetService);
    }

    @Test
    void genererBilletApresTransactionReussie_FailedTransaction_ReturnsNull() {
        dummyTransaction.setStatutTransaction(StatutTransaction.ECHEC);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);
        verifyNoInteractions(billetService);
    }

    @Test
    void genererBilletApresTransactionReussie_EmptyOffersInPanier_ReturnsNull() {
        dummyPanier.setContenuPaniers(new HashSet<>());
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);

        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);
        verify(billetService, times(0)).creerEtEnregistrerBillet(any(), anyList(), any(), any());
        verify(billetService, times(0)).finaliserBilletAvecQrCode(any());
    }

    @Test
    void genererBilletApresTransactionReussie_NullContenuPaniers_ReturnsNull() {
        dummyPanier.setContenuPaniers(null);
        when(billetService.genererCleAchat()).thenReturn(dummyCleAchat);
        when(billetService.genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat)).thenReturn(dummyCleFinaleBillet);

        Billet resultBillet = billetCreationService.genererBilletApresTransactionReussie(dummyPaiement);

        assertNull(resultBillet);

        verify(billetService, times(1)).genererCleAchat();
        verify(billetService, times(1)).genererCleFinaleBillet(dummyCleUtilisateur, dummyCleAchat);
        verify(billetService, times(0)).creerEtEnregistrerBillet(any(), anyList(), any(), any());
        verify(billetService, times(0)).finaliserBilletAvecQrCode(any());
    }
}