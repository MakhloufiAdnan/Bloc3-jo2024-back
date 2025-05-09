package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Billet;
import fr.studi.bloc3jo2024.entity.Paiement;

public interface BilletCreationService {

    /**
     * Génère et finalise un billet après une transaction de paiement réussie.
     *
     * @param paiement L'entité Paiement à succès.
     * @return L'entité Billet finalisée.
     */
    Billet genererBilletApresTransactionReussie(Paiement paiement);
}