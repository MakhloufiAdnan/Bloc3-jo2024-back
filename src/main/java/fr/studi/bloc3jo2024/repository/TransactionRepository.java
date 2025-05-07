package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Paiement;
import fr.studi.bloc3jo2024.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Recherche une transaction associée à un paiement spécifique.
     *
     * @param paiement Le paiement pour lequel rechercher la transaction.
     * @return Un Optional contenant la transaction correspondante si elle est trouvée, sinon un Optional vide.
     */
    Optional<Transaction> findByPaiement(Paiement paiement);
    Optional<Transaction> findByPaiementIdPaiement(Long payementId);
}