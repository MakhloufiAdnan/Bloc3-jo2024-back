package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {

    // Recherche d'un panier par son ID
    Panier findByIdPanier(Long idPanier);

    // Recherche de tous les paniers associés à un utilisateur, si nécessaire
    List<Panier> findByUtilisateurIdUtilisateur(UUID idUtilisateur);

    // Méthode qui permet de calculer le total d'un panier (en fonction des Offres)
    default double calculateTotal(Panier panier) {
        return panier.getOffres().stream()
                .mapToDouble(offre -> offre.getPrix() * offre.getQuantite())
                .sum();
    }
}
