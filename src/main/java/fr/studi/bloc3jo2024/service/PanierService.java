package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;
import fr.studi.bloc3jo2024.entity.Offre; // Ajout de l'import pour Offre

public interface PanierService {
    PanierDto getPanierUtilisateur(String utilisateurId);
    PanierDto ajouterOffreAuPanier(String utilisateurId, AjouterOffrePanierDto ajouterOffrePanierDto);
    PanierDto modifierQuantiteOffrePanier(String utilisateurId, ModifierContenuPanierDto modifierContenuPanierDto);
    PanierDto supprimerOffreDuPanier(String utilisateurId, Long offreId);
    PanierDto viderPanier(String utilisateurId);
    void supprimerOffreDeTousLesPaniers(Offre offre); // Type Offre corrigé
    PanierDto finaliserAchat(String utilisateurId);
}