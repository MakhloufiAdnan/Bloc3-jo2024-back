package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Paiement;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByPanier_idPanierAndUtilisateur_idUtilisateur(Long idPanier, UUID utilisateurId);

    @Query(
            "SELECT COUNT(p) " +
                    "FROM Paiement p " +
                    "JOIN p.panier pa " +
                    "JOIN pa.contenuPaniers cp " +
                    "WHERE cp.offre.idOffre = :offreId " +
                    "AND p.statutPaiement = :statutPaiement " +
                    "AND p.transaction.statutTransaction = :statutTransaction"
    )
    long countByOffreIdAndStatutPaiementAndTransaction_StatutTransaction(
                                                                          @Param("offreId") Long offreId,
                                                                          @Param("statutPaiement") StatutPaiement statutPaiement,
                                                                          @Param("statutTransaction") StatutTransaction statutTransaction
    );
    long countByStatutPaiementAndTransaction_StatutTransaction(StatutPaiement statutPaiement, StatutTransaction statutTransaction);

}