package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.dto.paiement.PaiementDto;
import fr.studi.bloc3jo2024.dto.paiement.PaiementSimulationResultDto;
import fr.studi.bloc3jo2024.dto.paiement.TransactionDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.PaiementRepository;
import fr.studi.bloc3jo2024.repository.PanierRepository;
import fr.studi.bloc3jo2024.repository.TransactionRepository;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import fr.studi.bloc3jo2024.repository.MethodePaiementRepository; // Ajout de l'import
import fr.studi.bloc3jo2024.service.BilletCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private PanierRepository panierRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private BilletCreationService billetCreationService;
    @Mock
    private MethodePaiementRepository methodePaiementRepository; // Mocker le nouveau repository

    @InjectMocks
    private PaiementServiceImpl paiementService;

    private UUID utilisateurId;
    private Long panierId;
    private Long paiementId;
    private Utilisateur utilisateur;
    private Panier panier;
    private Paiement paiement; // Entité Paiement de référence pour les tests
    private MethodePaiement methodeCarteBancaireEntity; // Entité MethodePaiement de référence
    private Transaction transaction;
    private Billet billet;
    private TransactionDto transactionDtoInitial;

    private final UUID utilisateurUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        utilisateurId = utilisateurUuid;
        panierId = 1L;
        paiementId = 10L;

        utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(utilisateurId);
        utilisateur.setCleUtilisateur("CLE_UTILISATEUR_TEST");

        panier = new Panier();
        panier.setIdPanier(panierId);
        panier.setUtilisateur(utilisateur);
        panier.setMontantTotal(BigDecimal.valueOf(150.0));

        // Créer une instance de MethodePaiement pour les tests
        methodeCarteBancaireEntity = new MethodePaiement();
        methodeCarteBancaireEntity.setIdMethode(1L); // ID factice pour le test
        methodeCarteBancaireEntity.setNomMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);

        paiement = new Paiement();
        paiement.setIdPaiement(paiementId);
        paiement.setUtilisateur(utilisateur);
        paiement.setPanier(panier);
        paiement.setMontant(panier.getMontantTotal());
        paiement.setMethodePaiement(methodeCarteBancaireEntity); // CORRECTION: Utiliser l'entité
        paiement.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiement.setDatePaiement(LocalDateTime.now());

        transaction = new Transaction();
        transaction.setIdTransaction(100L);
        transaction.setMontant(panier.getMontantTotal());
        transaction.setDateTransaction(paiement.getDatePaiement());
        transaction.setStatutTransaction(StatutTransaction.EN_ATTENTE);
        transaction.setPaiement(paiement);
        paiement.setTransaction(transaction);

        billet = new Billet();
        billet.setIdBillet(20L);
        billet.setCleFinaleBillet("CLE_FINALE_TEST");

        PaiementDto paiementDtoInitial;
        paiementDtoInitial = new PaiementDto();
        paiementDtoInitial.setIdPaiement(paiementId);
        paiementDtoInitial.setUtilisateurId(utilisateurId);
        paiementDtoInitial.setIdPanier(panierId);
        paiementDtoInitial.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoInitial.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE); // Le DTO utilise toujours l'Enum
        paiementDtoInitial.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiementDtoInitial.setDatePaiement(paiement.getDatePaiement());

        transactionDtoInitial = new TransactionDto();
        transactionDtoInitial.setIdTransaction(transaction.getIdTransaction());
        transactionDtoInitial.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoInitial.setDateTransaction(transaction.getDateTransaction());
        transactionDtoInitial.setStatutTransaction(StatutTransaction.EN_ATTENTE);

        paiementDtoInitial.setTransaction(transactionDtoInitial);
    }

    @Test
    void effectuerPaiement_Successful() {
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(panier));
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());

        // Mock pour la récupération de l'entité MethodePaiement
        when(methodePaiementRepository.findByNomMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE))
                .thenReturn(Optional.of(methodeCarteBancaireEntity));

        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            p.setIdPaiement(paiementId); // Simuler la sauvegarde et l'attribution d'un ID
            p.setTransaction(transaction); // S'assurer que la transaction est liée pour le mappage DTO
            return p;
        });
        PaiementDto expectedDto = new PaiementDto();
        expectedDto.setIdPaiement(paiementId);
        expectedDto.setUtilisateurId(utilisateurId);
        expectedDto.setIdPanier(panierId);
        expectedDto.setMontant(panier.getMontantTotal());
        expectedDto.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE); // Le DTO contient l'enum
        expectedDto.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        expectedDto.setDatePaiement(paiement.getDatePaiement()); // Utiliser une date fixe ou any() si elle est générée
        expectedDto.setTransaction(transactionDtoInitial);

        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(expectedDto);
        // mapPaiementToDto dans le service fait un second appel à modelMapper pour la transaction
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoInitial);


        PaiementDto resultDto = paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE);

        assertNotNull(resultDto);
        assertEquals(expectedDto.getIdPaiement(), resultDto.getIdPaiement());
        assertEquals(expectedDto.getStatutPaiement(), resultDto.getStatutPaiement());
        assertEquals(expectedDto.getMethodePaiement(), resultDto.getMethodePaiement());
        assertNotNull(resultDto.getTransaction());
        assertEquals(StatutTransaction.EN_ATTENTE, resultDto.getTransaction().getStatutTransaction());

        verify(utilisateurRepository).findById(utilisateurId);
        verify(panierRepository).findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository).findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(methodePaiementRepository).findByNomMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        verify(paiementRepository).save(any(Paiement.class));
        verify(modelMapper).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper).map(any(Transaction.class), eq(TransactionDto.class)); // Vérifié car mapPaiementToDto l'appelle
        verifyNoInteractions(billetCreationService);
    }

    @Test
    void effectuerPaiement_UtilisateurNotFound_ThrowsResourceNotFoundException() {
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals("Utilisateur non trouvé avec l'ID : " + utilisateurId, exception.getMessage());
        verify(methodePaiementRepository, never()).findByNomMethodePaiement(any()); // Ne doit pas être appelé
    }

    @Test
    void effectuerPaiement_PanierNotFound_ThrowsResourceNotFoundException() {
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals(String.format("Panier non trouvé avec l'ID : %d pour l'utilisateur ID : %s", panierId, utilisateurId), exception.getMessage());
        verify(methodePaiementRepository, never()).findByNomMethodePaiement(any());
    }

    @Test
    void effectuerPaiement_PaiementDejaExistant_ThrowsIllegalStateException() {
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(panier));
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(paiement));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals("Un paiement existe déjà pour ce panier et cet utilisateur.", exception.getMessage());
        verify(methodePaiementRepository, never()).findByNomMethodePaiement(any());
    }

    @Test
    void effectuerPaiement_MethodePaiementNotFound_ThrowsResourceNotFoundException() {
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(panier));
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());
        when(methodePaiementRepository.findByNomMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals("Méthode de paiement non trouvée : " + MethodePaiementEnum.CARTE_BANCAIRE, exception.getMessage());
    }


    @Test
    void simulerResultatPaiement_Successful() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement)); // paiement a methodeCarteBancaireEntity
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));
        when(billetCreationService.genererBilletApresTransactionReussie(paiement)).thenReturn(billet);

        PaiementDto paiementDtoSuccessful = new PaiementDto();
        paiementDtoSuccessful.setIdPaiement(paiementId);
        paiementDtoSuccessful.setUtilisateurId(utilisateurId);
        paiementDtoSuccessful.setIdPanier(panierId);
        paiementDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoSuccessful.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE); // DTO utilise Enum
        paiementDtoSuccessful.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiementDtoSuccessful.setDatePaiement(paiement.getDatePaiement());

        TransactionDto transactionDtoSuccessful = new TransactionDto();
        transactionDtoSuccessful.setIdTransaction(transaction.getIdTransaction());
        transactionDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoSuccessful.setDateTransaction(transaction.getDateTransaction());
        transactionDtoSuccessful.setStatutTransaction(StatutTransaction.REUSSI);
        transactionDtoSuccessful.setDetails("Détails de simulation réussie");
        paiementDtoSuccessful.setTransaction(transactionDtoSuccessful);

        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            PaiementDto dto = new PaiementDto();
            dto.setIdPaiement(p.getIdPaiement());
            dto.setUtilisateurId(p.getUtilisateur().getIdUtilisateur());
            dto.setIdPanier(p.getPanier().getIdPanier());
            dto.setMontant(p.getMontant());
            if (p.getMethodePaiement() != null) {
                dto.setMethodePaiement(p.getMethodePaiement().getNomMethodePaiement());
            }
            dto.setStatutPaiement(p.getStatutPaiement());
            dto.setDatePaiement(p.getDatePaiement());
            if (p.getTransaction() != null) {
                dto.setTransaction(modelMapper.map(p.getTransaction(), TransactionDto.class));
            }
            return dto;
        });
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoSuccessful);


        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, true, "Détails de simulation réussie");

        assertNotNull(resultDto);
        assertNotNull(resultDto.getPaiement());
        assertEquals(StatutPaiement.ACCEPTE, resultDto.getPaiement().getStatutPaiement());
        assertEquals(MethodePaiementEnum.CARTE_BANCAIRE, resultDto.getPaiement().getMethodePaiement());
        assertNotNull(resultDto.getPaiement().getTransaction());
        assertEquals(StatutTransaction.REUSSI, resultDto.getPaiement().getTransaction().getStatutTransaction());
        assertEquals(billet.getIdBillet(), resultDto.getBilletId());
        assertEquals(billet.getCleFinaleBillet(), resultDto.getCleFinaleBillet());

        verify(paiementRepository).save(paiement);
    }

    @Test
    void simulerResultatPaiement_Failed() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));

        PaiementDto paiementDtoFailed = new PaiementDto();
        // ... (configuration similaire à paiementDtoSuccessful mais avec statut REFUSE/ECHEC)
        paiementDtoFailed.setIdPaiement(paiementId);
        paiementDtoFailed.setUtilisateurId(utilisateurId);
        paiementDtoFailed.setIdPanier(panierId);
        paiementDtoFailed.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoFailed.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoFailed.setStatutPaiement(StatutPaiement.REFUSE);
        paiementDtoFailed.setDatePaiement(paiement.getDatePaiement());


        TransactionDto transactionDtoFailed = new TransactionDto();
        transactionDtoFailed.setIdTransaction(transaction.getIdTransaction());
        transactionDtoFailed.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoFailed.setDateTransaction(transaction.getDateTransaction());
        transactionDtoFailed.setStatutTransaction(StatutTransaction.ECHEC);
        transactionDtoFailed.setDetails("Détails de simulation échouée");

        paiementDtoFailed.setTransaction(transactionDtoFailed);


        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            PaiementDto dto = new PaiementDto();
            dto.setIdPaiement(p.getIdPaiement());
            dto.setUtilisateurId(p.getUtilisateur().getIdUtilisateur());
            dto.setIdPanier(p.getPanier().getIdPanier());
            dto.setMontant(p.getMontant());
            if (p.getMethodePaiement() != null) {
                dto.setMethodePaiement(p.getMethodePaiement().getNomMethodePaiement());
            }
            dto.setStatutPaiement(p.getStatutPaiement());
            dto.setDatePaiement(p.getDatePaiement());
            if (p.getTransaction() != null) {
                dto.setTransaction(modelMapper.map(p.getTransaction(), TransactionDto.class));
            }
            return dto;
        });
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoFailed);


        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, false, "Détails de simulation échouée");

        assertNotNull(resultDto);
        assertEquals(StatutPaiement.REFUSE, resultDto.getPaiement().getStatutPaiement());
        assertNull(resultDto.getBilletId());
        verify(billetCreationService, never()).genererBilletApresTransactionReussie(any());
    }

    @Test
    void simulerResultatPaiement_PaiementNotFound_ThrowsResourceNotFoundException() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                paiementService.simulerResultatPaiement(paiementId, true, "Details"));
    }

    @Test
    void simulerResultatPaiement_TransactionNotFound_ThrowsResourceNotFoundException() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                paiementService.simulerResultatPaiement(paiementId, true, "Details"));
    }

    @Test
    void simulerResultatPaiement_SuccessfulButBilletCreationReturnsNull() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));
        when(billetCreationService.genererBilletApresTransactionReussie(paiement)).thenReturn(null);

        PaiementDto paiementDtoSuccessful = new PaiementDto();
        // ... (configuration similaire à paiementDtoSuccessful)
        paiementDtoSuccessful.setIdPaiement(paiementId);
        paiementDtoSuccessful.setUtilisateurId(utilisateurId);
        paiementDtoSuccessful.setIdPanier(panierId);
        paiementDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoSuccessful.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoSuccessful.setStatutPaiement(StatutPaiement.ACCEPTE);
        paiementDtoSuccessful.setDatePaiement(paiement.getDatePaiement());

        TransactionDto transactionDtoSuccessful = new TransactionDto();
        transactionDtoSuccessful.setIdTransaction(transaction.getIdTransaction());
        transactionDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoSuccessful.setDateTransaction(transaction.getDateTransaction());
        transactionDtoSuccessful.setStatutTransaction(StatutTransaction.REUSSI);
        transactionDtoSuccessful.setDetails("Détails de simulation réussie");

        paiementDtoSuccessful.setTransaction(transactionDtoSuccessful);


        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            PaiementDto dto = new PaiementDto();
            dto.setIdPaiement(p.getIdPaiement());
            dto.setUtilisateurId(p.getUtilisateur().getIdUtilisateur());
            dto.setIdPanier(p.getPanier().getIdPanier());
            dto.setMontant(p.getMontant());
            if (p.getMethodePaiement() != null) {
                dto.setMethodePaiement(p.getMethodePaiement().getNomMethodePaiement());
            }
            dto.setStatutPaiement(p.getStatutPaiement());
            dto.setDatePaiement(p.getDatePaiement());
            if (p.getTransaction() != null) {
                dto.setTransaction(modelMapper.map(p.getTransaction(), TransactionDto.class));
            }
            return dto;
        });
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoSuccessful);


        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, true, "Détails de simulation réussie");

        assertNotNull(resultDto);
        assertEquals(StatutPaiement.ACCEPTE, resultDto.getPaiement().getStatutPaiement());
        assertNull(resultDto.getBilletId());
        assertNull(resultDto.getCleFinaleBillet());
    }

    @Test
    void getPaiementParPanier_Found_ReturnsOptionalPaiementDto() {
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(paiement));

        // Stubbing ModelMapper pour le mappage
        // Création d'un DTO qui correspond à l'état de 'paiement' dans setUp
        PaiementDto expectedDto = new PaiementDto();
        expectedDto.setIdPaiement(paiement.getIdPaiement());
        expectedDto.setUtilisateurId(paiement.getUtilisateur().getIdUtilisateur());
        expectedDto.setIdPanier(paiement.getPanier().getIdPanier());
        expectedDto.setMontant(paiement.getMontant());
        expectedDto.setMethodePaiement(paiement.getMethodePaiement().getNomMethodePaiement()); // Correctement depuis l'entité
        expectedDto.setStatutPaiement(paiement.getStatutPaiement());
        expectedDto.setDatePaiement(paiement.getDatePaiement());
        if (paiement.getTransaction() != null) {
            expectedDto.setTransaction(modelMapper.map(paiement.getTransaction(), TransactionDto.class));
        }

        when(modelMapper.map(paiement, PaiementDto.class)).thenReturn(expectedDto);
        if (paiement.getTransaction() != null) {
            when(modelMapper.map(paiement.getTransaction(), TransactionDto.class)).thenReturn(transactionDtoInitial);
        }


        Optional<PaiementDto> result = paiementService.getPaiementParPanier(utilisateurId, panierId);

        assertTrue(result.isPresent());
        assertEquals(expectedDto.getMethodePaiement(), result.get().getMethodePaiement());
    }

    @Test
    void getPaiementParPanier_NotFound_ReturnsEmptyOptional() {
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());

        Optional<PaiementDto> result = paiementService.getPaiementParPanier(utilisateurId, panierId);

        assertFalse(result.isPresent());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void getPaiementParId_Found_ReturnsOptionalPaiementDto() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        // Création d'un DTO qui correspond à l'état de 'paiement' dans setUp
        PaiementDto expectedDto = new PaiementDto();
        expectedDto.setIdPaiement(paiement.getIdPaiement());
        expectedDto.setUtilisateurId(paiement.getUtilisateur().getIdUtilisateur());
        expectedDto.setIdPanier(paiement.getPanier().getIdPanier());
        expectedDto.setMontant(paiement.getMontant());
        expectedDto.setMethodePaiement(paiement.getMethodePaiement().getNomMethodePaiement()); // Correctement depuis l'entité
        expectedDto.setStatutPaiement(paiement.getStatutPaiement());
        expectedDto.setDatePaiement(paiement.getDatePaiement());
        if (paiement.getTransaction() != null) {
            expectedDto.setTransaction(modelMapper.map(paiement.getTransaction(), TransactionDto.class));
        }

        when(modelMapper.map(paiement, PaiementDto.class)).thenReturn(expectedDto);
        if (paiement.getTransaction() != null) {
            when(modelMapper.map(paiement.getTransaction(), TransactionDto.class)).thenReturn(transactionDtoInitial);
        }


        Optional<PaiementDto> result = paiementService.getPaiementParId(paiementId);

        assertTrue(result.isPresent());
        assertEquals(expectedDto.getMethodePaiement(), result.get().getMethodePaiement());
    }

    @Test
    void getPaiementParId_NotFound_ReturnsEmptyOptional() {
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.empty());

        Optional<PaiementDto> result = paiementService.getPaiementParId(paiementId);

        assertFalse(result.isPresent());
        verify(modelMapper, never()).map(any(), any());
    }
}