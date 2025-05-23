package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto; // Importation du DTO
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for calculating and retrieving sales statistics.
 */
@Service
public class StatistiqueService {

    private static final Logger log = LoggerFactory.getLogger(StatistiqueService.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Calculates the number of successful sales for each offer type, aggregated by day.
     * This method is typically used to populate charts or reports showing sales trends over time.
     *
     * @return A list of {@link VenteParOffreDto}, where each DTO contains the date,
     * the offer type name, and the count of sales for that offer on that day.
     * Returns an empty list if no sales data is found or in case of an error.
     */
    public List<VenteParOffreDto> calculerVentesJournalieresParTypeOffre() {
        log.debug("Calculating daily sales per offer type (multi-day).");
        // Use the fully qualified name for the DTO and pass the enum directly
        String jpql = "SELECT NEW fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto(" +
                "DATE(t.dateTransaction), " +
                "o.typeOffre, " + // Pass enum directly, assuming JPA/DTO constructor handles it
                "COUNT(p)) " +
                "FROM Paiement p " +
                "JOIN p.transaction t " +
                "JOIN p.panier pa " +
                "JOIN pa.contenuPaniers cp " +
                "JOIN cp.offre o " +
                "WHERE p.statutPaiement = :statutPaiement " +
                "AND t.statutTransaction = :statutTransaction " +
                "GROUP BY DATE(t.dateTransaction), o.typeOffre " +
                "ORDER BY DATE(t.dateTransaction) ASC, o.typeOffre ASC";

        try {
            TypedQuery<VenteParOffreDto> query = entityManager.createQuery(jpql, VenteParOffreDto.class);
            query.setParameter("statutPaiement", StatutPaiement.ACCEPTE);
            query.setParameter("statutTransaction", StatutTransaction.REUSSI);
            List<VenteParOffreDto> result = query.getResultList();
            log.info("Retrieved {} records for daily sales statistics (multi-day).", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error calculating daily sales per offer type (multi-day).", e);
            return List.of();
        }
    }

    /**
     * Calculates the total number of successful sales for each offer type for a specific given day.
     *
     * @param jourDonne The specific date for which to calculate sales.
     * @return A list of {@link VenteParOffreDto}, where each DTO contains the specified date,
     * the offer type name, and the count of sales for that offer on that day.
     * Returns an empty list if no sales data is found for the given day or in case of an error.
     */
    public List<VenteParOffreDto> calculerVentesParOffrePourJourDonne(LocalDate jourDonne) {
        log.debug("Calculating sales per offer type for the day: {}", jourDonne);
        // Use the fully qualified name for the DTO and pass the enum directly
        String jpql = "SELECT NEW fr.studi.bloc3jo2024.dto.offres.VenteParOffreDto(" +
                ":jourDonneParam, " +
                "o.typeOffre, " +   // Pass enum directly
                "COUNT(p)) " +
                "FROM Paiement p " +
                "JOIN p.transaction t " +
                "JOIN p.panier pa " +
                "JOIN pa.contenuPaniers cp " +
                "JOIN cp.offre o " +
                "WHERE p.statutPaiement = :statutPaiement " +
                "AND t.statutTransaction = :statutTransaction " +
                "AND DATE(t.dateTransaction) = :jourDonneParam " +
                "GROUP BY o.typeOffre " +
                "ORDER BY o.typeOffre ASC";

        try {
            TypedQuery<VenteParOffreDto> query = entityManager.createQuery(jpql, VenteParOffreDto.class);
            query.setParameter("statutPaiement", StatutPaiement.ACCEPTE);
            query.setParameter("statutTransaction", StatutTransaction.REUSSI);
            query.setParameter("jourDonneParam", jourDonne);
            List<VenteParOffreDto> result = query.getResultList();
            log.info("Retrieved {} records for sales statistics for the day {}.", result.size(), jourDonne);
            return result;
        } catch (Exception e) {
            log.error("Error calculating sales per offer type for the day {}.", jourDonne, e);
            return List.of();
        }
    }
}