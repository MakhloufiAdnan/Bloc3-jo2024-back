package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.panier.AjouterOffrePanierDto;
import fr.studi.bloc3jo2024.dto.panier.ModifierContenuPanierDto;
import fr.studi.bloc3jo2024.dto.panier.PanierDto;

public interface PanierService {
    PanierDto getPanierUtilisateur(String utilisateurId); // Récupérer le panier de l'utilisateur
    PanierDto ajouterOffreAuPanier(String utilisateurId, AjouterOffrePanierDto ajouterOffrePanierDto);
    PanierDto modifierQuantiteOffrePanier(String utilisateurId, ModifierContenuPanierDto modifierContenuPanierDto);
    PanierDto supprimerOffreDuPanier(String utilisateurId, Long offreId);
    PanierDto viderPanier(String utilisateurId);
    void supprimerOffreDeTousLesPaniers(fr.studi.bloc3jo2024.entity.Offre offre);
    PanierDto finaliserAchat(String utilisateurId); // Modifiez la signature pour prendre l'ID utilisateur
}