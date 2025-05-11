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

    @InjectMocks
    private PaiementServiceImpl paiementService;

    // Données de test (objets réels, PAS des mocks)
    private UUID utilisateurId;
    private Long panierId;
    private Long paiementId;
    private Utilisateur utilisateur;
    private Panier panier;
    private Paiement paiement;
    private Transaction transaction;
    private Billet billet;
    // Les DTOs de simulation sont créés dans les tests spécifiques car leur statut dépend du résultat simulé
    private PaiementDto paiementDtoInitial;
    private TransactionDto transactionDtoInitial;

    private final UUID utilisateurUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Configurer les données de test communes (objets réels)
        utilisateurId = utilisateurUuid; // Utiliser l'UUID généré
        panierId = 1L;
        paiementId = 10L;

        utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(utilisateurId);
        utilisateur.setCleUtilisateur("CLE_UTILISATEUR_TEST");

        panier = new Panier();
        panier.setIdPanier(panierId);
        panier.setUtilisateur(utilisateur); // Lier le panier à l'utilisateur
        panier.setMontantTotal(BigDecimal.valueOf(150.0));

        paiement = new Paiement();
        paiement.setIdPaiement(paiementId);
        paiement.setUtilisateur(utilisateur); // Lier le paiement à l'utilisateur
        paiement.setPanier(panier); // Lier le paiement au panier
        paiement.setMontant(panier.getMontantTotal());
        paiement.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiement.setStatutPaiement(StatutPaiement.EN_ATTENTE); // Statut initial
        paiement.setDatePaiement(LocalDateTime.now());

        transaction = new Transaction();
        transaction.setIdTransaction(100L);
        transaction.setMontant(panier.getMontantTotal());
        transaction.setDateTransaction(paiement.getDatePaiement());
        transaction.setStatutTransaction(StatutTransaction.EN_ATTENTE); // Statut initial
        transaction.setPaiement(paiement); // Lier la transaction au paiement
        paiement.setTransaction(transaction); // Lier le paiement à la transaction

        billet = new Billet();
        billet.setIdBillet(20L);
        billet.setCleFinaleBillet("CLE_FINALE_TEST");

        // DTOs initiaux pour les tests qui n'impliquent pas de changement de statut de paiement
        paiementDtoInitial = new PaiementDto();
        paiementDtoInitial.setIdPaiement(paiementId);
        paiementDtoInitial.setUtilisateurId(utilisateurId);
        paiementDtoInitial.setIdPanier(panierId);
        paiementDtoInitial.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoInitial.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoInitial.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiementDtoInitial.setDatePaiement(paiement.getDatePaiement());

        transactionDtoInitial = new TransactionDto();
        transactionDtoInitial.setIdTransaction(transaction.getIdTransaction());
        transactionDtoInitial.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoInitial.setDateTransaction(transaction.getDateTransaction());
        transactionDtoInitial.setStatutTransaction(StatutTransaction.EN_ATTENTE);

        paiementDtoInitial.setTransaction(transactionDtoInitial);

        PaiementSimulationResultDto simulationResultDtoInitial;
        simulationResultDtoInitial = new PaiementSimulationResultDto();
        simulationResultDtoInitial.setPaiement(paiementDtoInitial);
    }

    // --- Tests pour effectuerPaiement ---

    @Test
    void effectuerPaiement_Successful() {
        // Arrange (Préparer)
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(panier));
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty()); // Aucun paiement existant
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiement); // Retourner le paiement créé

        // Stubbing ModelMapper pour le mappage final (statut EN_ATTENTE car c'est le statut initial)
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoInitial);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoInitial);

        // Act
        PaiementDto resultDto = paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE);

        // Assert
        assertNotNull(resultDto);
        assertEquals(paiementDtoInitial, resultDto); // Comparer avec le DTO initial (EN_ATTENTE)
        assertEquals(StatutPaiement.EN_ATTENTE, resultDto.getStatutPaiement());
        assertNotNull(resultDto.getTransaction());
        assertEquals(StatutTransaction.EN_ATTENTE, resultDto.getTransaction().getStatutTransaction());


        // Vérifier les interactions avec les dépôts
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository, times(1)).findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository, times(1)).save(any(Paiement.class)); // Vérifie qu'un paiement a été sauvegardé

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));

        // Aucune interaction avec BilletCreationService lors du paiement initial
        verifyNoInteractions(billetCreationService);
    }

    @Test
    void effectuerPaiement_UtilisateurNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals("Utilisateur non trouvé avec l'ID : " + utilisateurId, exception.getMessage());

        // Vérifier les interactions avec les dépôts
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(0)).findByIdPanierAndUtilisateur_idUtilisateur(anyLong(), any(UUID.class));
        verify(paiementRepository, times(0)).findByPanier_idPanierAndUtilisateur_idUtilisateur(anyLong(), any(UUID.class));
        verify(paiementRepository, times(0)).save(any(Paiement.class));
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(billetCreationService);
    }

    @Test
    void effectuerPaiement_PanierNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals(String.format("Panier non trouvé avec l'ID : %d pour l'utilisateur ID : %s", panierId, utilisateurId), exception.getMessage());

        // Vérifier les interactions avec les dépôts
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository, times(0)).findByPanier_idPanierAndUtilisateur_idUtilisateur(anyLong(), any(UUID.class));
        verify(paiementRepository, times(0)).save(any(Paiement.class));
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(billetCreationService);
    }

    @Test
    void effectuerPaiement_PaiementDejaExistant_ThrowsIllegalStateException() {
        // Arrange
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(panierRepository.findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(panier));
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(paiement)); // Paiement existant trouvé

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                paiementService.effectuerPaiement(utilisateurId, panierId, MethodePaiementEnum.CARTE_BANCAIRE));

        assertEquals("Un paiement existe déjà pour ce panier et cet utilisateur.", exception.getMessage());

        // Vérifier les interactions avec les dépôts
        verify(utilisateurRepository, times(1)).findById(utilisateurId);
        verify(panierRepository, times(1)).findByIdPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository, times(1)).findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);
        verify(paiementRepository, times(0)).save(any(Paiement.class)); // Aucune sauvegarde ne devrait avoir lieu
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(modelMapper);
        verifyNoInteractions(billetCreationService);
    }

    // --- Tests pour simulerResultatPaiement ---

    @Test
    void simulerResultatPaiement_Successful() {
        // Arrange (Préparer)
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));
        when(billetCreationService.genererBilletApresTransactionReussie(paiement)).thenReturn(billet);

        // Créer les DTOs avec les statuts attendus après une simulation réussie
        PaiementDto paiementDtoSuccessful = new PaiementDto();
        paiementDtoSuccessful.setIdPaiement(paiementId);
        paiementDtoSuccessful.setUtilisateurId(utilisateurId);
        paiementDtoSuccessful.setIdPanier(panierId);
        paiementDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoSuccessful.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoSuccessful.setStatutPaiement(StatutPaiement.ACCEPTE); // Statut attendu
        paiementDtoSuccessful.setDatePaiement(paiement.getDatePaiement()); // Utiliser la date du paiement créé

        TransactionDto transactionDtoSuccessful = new TransactionDto();
        transactionDtoSuccessful.setIdTransaction(transaction.getIdTransaction());
        transactionDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoSuccessful.setDateTransaction(transaction.getDateTransaction());
        transactionDtoSuccessful.setStatutTransaction(StatutTransaction.REUSSI); // Statut attendu
        transactionDtoSuccessful.setDetails("Détails de simulation réussie"); // Détails attendus

        paiementDtoSuccessful.setTransaction(transactionDtoSuccessful);

        // Stubbing ModelMapper pour retourner les DTOs avec les statuts mis à jour
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoSuccessful);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoSuccessful);


        // Act (Agir)
        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, true, "Détails de simulation réussie");

        // Assert (Vérifier)
        assertNotNull(resultDto);
        assertNotNull(resultDto.getPaiement());
        assertEquals(paiementDtoSuccessful, resultDto.getPaiement()); // Comparer avec le DTO attendu (ACCEPTE)
        assertEquals(StatutPaiement.ACCEPTE, resultDto.getPaiement().getStatutPaiement());
        assertNotNull(resultDto.getPaiement().getTransaction());
        assertEquals(StatutTransaction.REUSSI, resultDto.getPaiement().getTransaction().getStatutTransaction());
        assertEquals("Détails de simulation réussie", resultDto.getPaiement().getTransaction().getDetails());
        assertNotNull(resultDto.getBilletId()); // Vérifier que les détails du billet sont présents
        assertNotNull(resultDto.getCleFinaleBillet());
        assertEquals(billet.getIdBillet(), resultDto.getBilletId());
        assertEquals(billet.getCleFinaleBillet(), resultDto.getCleFinaleBillet());


        // Vérifier les interactions avec les dépôts
        verify(paiementRepository, times(1)).findById(paiementId);
        verify(transactionRepository, times(1)).findByPaiementIdPaiement(paiementId);
        verify(transactionRepository, times(1)).save(transaction); // La transaction est explicitement sauvegardée

        // Vérifier les interactions avec les services
        verify(billetCreationService, times(1)).genererBilletApresTransactionReussie(paiement);

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));
    }

    @Test
    void simulerResultatPaiement_Failed() {
        // Arrange (Préparer)
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));

        // Créer les DTOs avec les statuts attendus après une simulation échouée
        PaiementDto paiementDtoFailed = new PaiementDto();
        paiementDtoFailed.setIdPaiement(paiementId);
        paiementDtoFailed.setUtilisateurId(utilisateurId);
        paiementDtoFailed.setIdPanier(panierId);
        paiementDtoFailed.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoFailed.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoFailed.setStatutPaiement(StatutPaiement.REFUSE); // Statut attendu
        paiementDtoFailed.setDatePaiement(paiement.getDatePaiement()); // Utiliser la date du paiement créé

        TransactionDto transactionDtoFailed = new TransactionDto();
        transactionDtoFailed.setIdTransaction(transaction.getIdTransaction());
        transactionDtoFailed.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoFailed.setDateTransaction(transaction.getDateTransaction());
        transactionDtoFailed.setStatutTransaction(StatutTransaction.ECHEC); // Statut attendu
        transactionDtoFailed.setDetails("Détails de simulation échouée"); // Détails attendus

        paiementDtoFailed.setTransaction(transactionDtoFailed);

        // Stubbing ModelMapper pour retourner les DTOs avec les statuts mis à jour
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoFailed);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoFailed);


        // Act (Agir)
        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, false, "Détails de simulation échouée");

        // Assert (Vérifier)
        assertNotNull(resultDto);
        assertNotNull(resultDto.getPaiement());
        assertEquals(paiementDtoFailed, resultDto.getPaiement()); // Comparer avec le DTO attendu (REFUSE)
        assertEquals(StatutPaiement.REFUSE, resultDto.getPaiement().getStatutPaiement());
        assertNotNull(resultDto.getPaiement().getTransaction());
        assertEquals(StatutTransaction.ECHEC, resultDto.getPaiement().getTransaction().getStatutTransaction());
        assertEquals("Détails de simulation échouée", resultDto.getPaiement().getTransaction().getDetails());
        assertNull(resultDto.getBilletId()); // Vérifier que les détails du billet ne sont PAS présents
        assertNull(resultDto.getCleFinaleBillet());


        // Vérifier les interactions avec les dépôts
        verify(paiementRepository, times(1)).findById(paiementId);
        verify(transactionRepository, times(1)).findByPaiementIdPaiement(paiementId);
        verify(transactionRepository, times(1)).save(transaction); // La transaction est explicitement sauvegardée

        // Vérifier les interactions avec les services - La création de billet n'est PAS appelée en cas d'échec
        verifyNoInteractions(billetCreationService);

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));
    }

    @Test
    void simulerResultatPaiement_PaiementNotFound_ThrowsResourceNotFoundException() {
        // Arrange (Préparer)
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.empty());

        // Act & Assert (Agir et Vérifier)
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.simulerResultatPaiement(paiementId, true, "Details"));

        assertEquals("Paiement non trouvé avec l'ID : " + paiementId, exception.getMessage());

        // Vérifier les interactions avec les dépôts
        verify(paiementRepository, times(1)).findById(paiementId);
        verify(transactionRepository, times(0)).findByPaiementIdPaiement(anyLong()); // La recherche de transaction n'est pas atteinte
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        // Vérifier les interactions avec les services
        verifyNoInteractions(billetCreationService);

        // Vérifier les interactions avec ModelMapper
        verifyNoInteractions(modelMapper);
    }

    @Test
    void simulerResultatPaiement_TransactionNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                paiementService.simulerResultatPaiement(paiementId, true, "Details"));

        assertEquals("Transaction non trouvée pour le paiement ID : " + paiementId, exception.getMessage());

        // Vérifier les interactions avec les dépôts
        verify(paiementRepository, times(1)).findById(paiementId);
        verify(transactionRepository, times(1)).findByPaiementIdPaiement(paiementId);
        verify(transactionRepository, times(0)).save(any(Transaction.class)); // La transaction n'est pas sauvegardée

        // Vérifier les interactions avec les services
        verifyNoInteractions(billetCreationService);

        // Vérifier les interactions avec ModelMapper
        verifyNoInteractions(modelMapper);
    }

    @Test
    void simulerResultatPaiement_SuccessfulButBilletCreationReturnsNull() {
        // Arrange (Préparer)
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(transactionRepository.findByPaiementIdPaiement(paiementId)).thenReturn(Optional.of(transaction));
        when(billetCreationService.genererBilletApresTransactionReussie(paiement)).thenReturn(null); // La création de billet retourne null

        // Créer les DTOs avec les statuts attendus après une simulation réussie (même si billet est null)
        PaiementDto paiementDtoSuccessful = new PaiementDto();
        paiementDtoSuccessful.setIdPaiement(paiementId);
        paiementDtoSuccessful.setUtilisateurId(utilisateurId);
        paiementDtoSuccessful.setIdPanier(panierId);
        paiementDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        paiementDtoSuccessful.setMethodePaiement(MethodePaiementEnum.CARTE_BANCAIRE);
        paiementDtoSuccessful.setStatutPaiement(StatutPaiement.ACCEPTE); // Statut attendu
        paiementDtoSuccessful.setDatePaiement(paiement.getDatePaiement()); // Utiliser la date du paiement créé

        TransactionDto transactionDtoSuccessful = new TransactionDto();
        transactionDtoSuccessful.setIdTransaction(transaction.getIdTransaction());
        transactionDtoSuccessful.setMontant(BigDecimal.valueOf(150.0));
        transactionDtoSuccessful.setDateTransaction(transaction.getDateTransaction());
        transactionDtoSuccessful.setStatutTransaction(StatutTransaction.REUSSI); // Statut attendu
        transactionDtoSuccessful.setDetails("Détails de simulation réussie"); // Détails attendus

        paiementDtoSuccessful.setTransaction(transactionDtoSuccessful);


        // Stubbing ModelMapper pour le mappage final
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoSuccessful);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoSuccessful);


        // Act
        PaiementSimulationResultDto resultDto = paiementService.simulerResultatPaiement(paiementId, true, "Détails de simulation réussie");

        // Assert
        assertNotNull(resultDto);
        assertNotNull(resultDto.getPaiement());
        assertEquals(paiementDtoSuccessful, resultDto.getPaiement()); // Comparer avec le DTO attendu (ACCEPTE)
        assertEquals(StatutPaiement.ACCEPTE, resultDto.getPaiement().getStatutPaiement());
        assertNotNull(resultDto.getPaiement().getTransaction());
        assertEquals(StatutTransaction.REUSSI, resultDto.getPaiement().getTransaction().getStatutTransaction());
        assertEquals("Détails de simulation réussie", resultDto.getPaiement().getTransaction().getDetails());
        assertNull(resultDto.getBilletId()); // Vérifier que les détails du billet ne sont PAS présents
        assertNull(resultDto.getCleFinaleBillet());

        // Vérifier les interactions avec les dépôts
        verify(paiementRepository, times(1)).findById(paiementId);
        verify(transactionRepository, times(1)).findByPaiementIdPaiement(paiementId);
        verify(transactionRepository, times(1)).save(transaction); // La transaction est explicitement sauvegardée

        // Vérifier les interactions avec les services
        verify(billetCreationService, times(1)).genererBilletApresTransactionReussie(paiement); // Le service de création de billet a été appelé

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));
    }


    // --- Tests pour getPaiementParPanier ---

    @Test
    void getPaiementParPanier_Found_ReturnsOptionalPaiementDto() {
        // Arrange
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.of(paiement));
        // Stubbing ModelMapper pour le mappage (utilise les DTOs initiaux car pas de changement de statut ici)
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoInitial);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoInitial);

        // Act
        Optional<PaiementDto> result = paiementService.getPaiementParPanier(utilisateurId, panierId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(paiementDtoInitial, result.get()); // Comparer avec le DTO initial (EN_ATTENTE)
        assertEquals(StatutPaiement.EN_ATTENTE, result.get().getStatutPaiement()); // Vérification explicite

        // Vérifier les interactions avec le dépôt
        verify(paiementRepository, times(1)).findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));

        // Aucune interaction avec les autres mocks
        verifyNoInteractions(utilisateurRepository);
        verifyNoInteractions(panierRepository);
        verifyNoInteractions(billetCreationService);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void getPaiementParPanier_NotFound_ReturnsEmptyOptional() {
        // Arrange
        when(paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId)).thenReturn(Optional.empty());

        // Act
        Optional<PaiementDto> result = paiementService.getPaiementParPanier(utilisateurId, panierId);

        // Assert
        assertFalse(result.isPresent());

        // Vérifier les interactions avec le dépôt
        verify(paiementRepository, times(1)).findByPanier_idPanierAndUtilisateur_idUtilisateur(panierId, utilisateurId);

        // Vérifier que ModelMapper n'est PAS utilisé
        verifyNoInteractions(modelMapper);

        // Aucune interaction avec les autres mocks
        verifyNoInteractions(utilisateurRepository);
        verifyNoInteractions(panierRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(billetCreationService);
    }

    // --- Tests pour getPaiementParId ---

    @Test
    void getPaiementParId_Found_ReturnsOptionalPaiementDto() {
        // Arrange
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        // Stubbing ModelMapper pour le mappage (utilise les DTOs initiaux car pas de changement de statut ici)
        when(modelMapper.map(any(Paiement.class), eq(PaiementDto.class))).thenReturn(paiementDtoInitial);
        when(modelMapper.map(any(Transaction.class), eq(TransactionDto.class))).thenReturn(transactionDtoInitial);


        // Act
        Optional<PaiementDto> result = paiementService.getPaiementParId(paiementId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(paiementDtoInitial, result.get()); // Comparer avec le DTO initial (EN_ATTENTE)
        assertEquals(StatutPaiement.EN_ATTENTE, result.get().getStatutPaiement()); // Vérification explicite


        // Vérifier les interactions avec le dépôt
        verify(paiementRepository, times(1)).findById(paiementId);

        // Vérifier les interactions avec ModelMapper
        verify(modelMapper, times(1)).map(any(Paiement.class), eq(PaiementDto.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionDto.class));


        // Aucune interaction avec les autres mocks
        verifyNoInteractions(utilisateurRepository);
        verifyNoInteractions(panierRepository);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verifyNoInteractions(billetCreationService);
    }

    @Test
    void getPaiementParId_NotFound_ReturnsEmptyOptional() {
        // Arrange
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.empty());

        // Act
        Optional<PaiementDto> result = paiementService.getPaiementParId(paiementId);

        // Assert
        assertFalse(result.isPresent());

        // Vérifier les interactions avec le dépôt
        verify(paiementRepository, times(1)).findById(paiementId);

        // Vérifier que ModelMapper n'est PAS utilisé
        verifyNoInteractions(modelMapper);

        // Aucune interaction avec les autres mocks
        verifyNoInteractions(utilisateurRepository);
        verifyNoInteractions(panierRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(billetCreationService);
    }
}