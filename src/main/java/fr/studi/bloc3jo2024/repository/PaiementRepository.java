package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {


    Optional<Paiement> findByPanier_idPanierAndUtilisateur_idUtilisateur(Long idPanier, UUID utilisateurId);
}