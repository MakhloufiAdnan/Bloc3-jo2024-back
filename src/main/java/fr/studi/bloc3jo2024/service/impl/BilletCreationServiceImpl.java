package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import fr.studi.bloc3jo2024.service.BilletCreationService;
import fr.studi.bloc3jo2024.service.BilletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BilletCreationServiceImpl implements BilletCreationService {

    private static final Logger logger = LoggerFactory.getLogger(BilletCreationServiceImpl.class);

    private final BilletService billetService;

    @Override
    @Transactional
    public Billet genererBilletApresTransactionReussie(Paiement paiement) {
        logger.info("Déclenchement de la génération du billet pour paiement ID : {}", paiement.getIdPaiement());

        Utilisateur utilisateur = paiement.getUtilisateur();
        if (utilisateur == null) {
            logger.error("Cannot generate billet for paiement ID {} - User is null.", paiement.getIdPaiement());
            return null;
        }

        Panier panier = paiement.getPanier();
        if (panier == null) {
            logger.error("Cannot generate billet for paiement ID {} - Panier is null.", paiement.getIdPaiement());
            return null;
        }

        Transaction transaction = paiement.getTransaction();
        if (transaction == null || transaction.getStatutTransaction() != StatutTransaction.REUSSI) {
            logger.error("Cannot generate billet for paiement ID {} - Associated transaction is null or not successful.", paiement.getIdPaiement());
            return null;
        }

        LocalDateTime dateValidation = transaction.getDateValidation();
        if (dateValidation == null) {
            logger.warn("Transaction ID {} for paiement ID {} has no validation date. Using current time for billet creation.", transaction.getIdTransaction(), paiement.getIdPaiement());
            dateValidation = LocalDateTime.now(); // Fallback
        }

        // Utilisez BilletService pour la génération de clés et les opérations de billetterie
        String cleAchat = billetService.genererCleAchat();
        String cleUtilisateur = utilisateur.getCleUtilisateur();
        String cleFinaleBillet = billetService.genererCleFinaleBillet(cleUtilisateur, cleAchat);

        // Obtenir des offres depuis le contenuPaniers du panier
        List<Offre> offres = panier.getContenuPaniers() != null ?
                panier.getContenuPaniers().stream()
                        .map(ContenuPanier::getOffre)
                        .toList() : List.of();

        if (offres.isEmpty()) {
            logger.warn("No offers found in panier ID {} for billet generation.", panier.getIdPanier());
            return null;
        }

        // Créer et enregistrer le billet
        Billet billet = billetService.creerEtEnregistrerBillet(utilisateur, offres, cleFinaleBillet, dateValidation);

        Billet finalBillet = billetService.finaliserBilletAvecQrCode(billet); // Ajouter un QR, enregistrer à nouveau, envoyer un e-mail

        logger.info("Billet ID : {} créé pour paiement ID : {}", finalBillet != null ? finalBillet.getIdBillet() : "N/A", paiement.getIdPaiement());
        return finalBillet;
    }
}