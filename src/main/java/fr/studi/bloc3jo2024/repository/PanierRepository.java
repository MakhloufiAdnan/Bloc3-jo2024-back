package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Panier;
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
}
