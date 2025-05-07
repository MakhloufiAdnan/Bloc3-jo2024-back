package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Panier;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {

    Optional<Panier> findByIdPanierAndUtilisateur_idUtilisateur(Long idPanier, UUID utilisateurId);
    Optional<Panier> findByUtilisateur_idUtilisateurAndStatut(UUID utilisateurId, StatutPanier statut);
}