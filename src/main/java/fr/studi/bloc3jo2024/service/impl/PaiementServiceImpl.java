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
import fr.studi.bloc3jo2024.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private static final Logger logger = LoggerFactory.getLogger(PaiementServiceImpl.class);

    private final PaiementRepository paiementRepository;
    private final PanierRepository panierRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;

    private final BilletCreationService billetCreationService;
    private static final String UTILISATEUR_NOT_FOUND = "Utilisateur non trouvé avec l'ID : ";
    private static final String PAIEMENT_DEJA_EXISTANT = "Un paiement existe déjà pour ce panier et cet utilisateur.";
    private static final String PAIEMENT_NOT_FOUND = "Paiement non trouvé avec l'ID : ";

    @Override
    @Transactional
    public PaiementDto effectuerPaiement(UUID utilisateurId, Long idPanier, MethodePaiementEnum methodePaiement) {
        logger.info("Tentative d'effectuer un paiement pour l'utilisateur ID : {}, panier ID : {}, méthode : {}", utilisateurId, idPanier, methodePaiement);

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(UTILISATEUR_NOT_FOUND + utilisateurId));

        Panier panier = panierRepository.findByIdPanierAndUtilisateur_IdUtilisateur(idPanier, utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Panier non trouvé avec l'ID : %d pour l'utilisateur ID : %s", idPanier, utilisateurId)
                ));

        if (paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(idPanier, utilisateurId).isPresent()) {
            logger.warn("Un paiement existe déjà pour le panier ID : {} et l'utilisateur ID : {}", idPanier, utilisateurId);
            throw new IllegalStateException(PAIEMENT_DEJA_EXISTANT);
        }

        Paiement paiement = new Paiement();
        paiement.setUtilisateur(utilisateur);
        paiement.setPanier(panier);
        paiement.setMontant(panier.getMontantTotal());
        paiement.setMethodePaiement(methodePaiement);
        paiement.setStatutPaiement(StatutPaiement.EN_ATTENTE);
        paiement.setDatePaiement(LocalDateTime.now());

        Transaction transaction = new Transaction();
        transaction.setMontant(panier.getMontantTotal());
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setStatutTransaction(StatutTransaction.EN_ATTENTE);
        transaction.setPaiement(paiement);
        paiement.setTransaction(transaction);

        paiementRepository.save(paiement);

        logger.info("Paiement initialisé avec l'ID : {} pour l'utilisateur ID : {}, panier ID : {}", paiement.getIdPaiement(), utilisateurId, idPanier);
        return mapPaiementToDto(paiement);
    }

    @Override
    @Transactional
    public PaiementSimulationResultDto simulerResultatPaiement(Long idPaiement, boolean paiementReussi, String detailsSimules) {
        logger.info("Simulation du résultat du paiement ID : {}, résultat : {}", idPaiement, paiementReussi);
        Paiement paiement = paiementRepository.findById(idPaiement)
                .orElseThrow(() -> new ResourceNotFoundException(PAIEMENT_NOT_FOUND + idPaiement));
        Transaction transaction = transactionRepository.findByPaiementIdPaiement(idPaiement)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction non trouvée pour le paiement ID : " + idPaiement));

        paiement.setStatutPaiement(paiementReussi ? StatutPaiement.ACCEPTE : StatutPaiement.REFUSE);

        transaction.setStatutTransaction(paiementReussi ? StatutTransaction.REUSSI : StatutTransaction.ECHEC);
        transaction.setDateValidation(paiementReussi ? LocalDateTime.now() : null);
        transaction.setDetails(detailsSimules);
        transactionRepository.save(transaction); // Sauvegarde la transaction (et potentiellement le paiement via cascade)
        logger.info("Résultat du paiement ID : {} mis à jour, statut : {}", idPaiement, paiement.getStatutPaiement());

        Billet billetCree = null; // Variable pour stocker le billet créé

        // Générer le billet si la transaction est réussie
        if (transaction.getStatutTransaction() == StatutTransaction.REUSSI) {
            billetCree = billetCreationService.genererBilletApresTransactionReussie(paiement);
            if (billetCree != null) {
                logger.info("Billet créé avec ID : {} pour paiement ID : {}", billetCree.getIdBillet(), idPaiement);
            } else {
                logger.warn("Billet creation service returned null for paiement ID {}", paiement.getIdPaiement());
            }
        }
        // Mapper le Paiement en DTO
        PaiementDto paiementDto = mapPaiementToDto(paiement);

        // Créer le DTO de résultat incluant le billet
        PaiementSimulationResultDto resultDto = new PaiementSimulationResultDto();
        resultDto.setPaiement(paiementDto);

        if (billetCree != null) {
            resultDto.setBilletId(billetCree.getIdBillet());
            resultDto.setCleFinaleBillet(billetCree.getCleFinaleBillet());
        }

        return resultDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaiementDto> getPaiementParPanier(UUID utilisateurId, Long idPanier) {
        logger.info("Récupération du paiement pour l'utilisateur ID : {}, panier ID : {}", utilisateurId, idPanier);
        return paiementRepository.findByPanier_idPanierAndUtilisateur_idUtilisateur(idPanier, utilisateurId)
                .map(this::mapPaiementToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaiementDto> getPaiementParId(Long idPaiement) {
        logger.info("Récupération du paiement avec l'ID : {}", idPaiement);
        return paiementRepository.findById(idPaiement)
                .map(this::mapPaiementToDto);
    }

    private PaiementDto mapPaiementToDto(Paiement paiement) {
        PaiementDto paiementDto = modelMapper.map(paiement, PaiementDto.class);
        if (paiement.getUtilisateur() != null) {
            paiementDto.setUtilisateurId(paiement.getUtilisateur().getIdUtilisateur());
        } else {
            logger.warn("Paiement ID {} has no associated user when mapping to DTO.", paiement.getIdPaiement());
        }

        if (paiement.getTransaction() != null) {
            paiementDto.setTransaction(modelMapper.map(paiement.getTransaction(), TransactionDto.class));
        }
        return paiementDto;
    }
}