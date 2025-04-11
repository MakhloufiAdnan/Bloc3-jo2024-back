package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.Panier;
import fr.bloc_jo2024.repository.PanierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class PanierService {

    @Autowired
    private PanierRepository panierRepository;

    // Méthode pour recalculer le montant total dans un panier
    @Transactional
    public void recalculerMontantTotal(Panier panier) {

        // J'accède à la collection des offres dans le panier
        double total = panier.getOffres().stream()
                .mapToDouble(offre -> offre.getPrix() * offre.getQuantite())
                .sum(); // Je calcule la somme des prix * quantités

        if (total < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif.");
        }

        panier.setMontantTotal(total); // Mise à jour du montant total dans le panier
    }

    // Méthode pour ajouter une offre au panier
    @Transactional
    public Panier ajouterOffre(Panier panier, Offre offre) {

        // Ajoute l'offre à la liste des offres dans le panier
        panier.getOffres().add(offre);

        // Recalcul du montant total après l'ajout de l'offre
        recalculerMontantTotal(panier);

        // Sauvegarde le panier avec la nouvelle offre et le montant mis à jour
        return panierRepository.save(panier);
    }

    // Méthode pour retirer une offre du panier
    @Transactional
    public Panier retirerOffre(Panier panier, Offre offre) {

        // Retire l'offre de la liste des offres dans le panier
        panier.getOffres().remove(offre);

        // Recalcul du montant total après le retrait de l'offre
        recalculerMontantTotal(panier);

        // Sauvegarde le panier avec la nouvelle liste d'offres et le montant mis à jour
        return panierRepository.save(panier);
    }
}