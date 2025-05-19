package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Panier;
import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {

    /**
     * Trouve un panier par son ID et l'ID de l'utilisateur associé.
     * @param idPanier ID du panier.
     * @param utilisateurId ID de l'utilisateur.
     * @return Un Optional de Panier.
     */
    Optional<Panier> findByIdPanierAndUtilisateur_IdUtilisateur(Long idPanier, UUID utilisateurId);

    /**
     * Trouve un panier par l'ID de l'utilisateur et le statut du panier.
     * @param utilisateurId ID de l'utilisateur.
     * @param statut Statut du panier.
     * @return Un Optional de Panier.
     */
    @Query("SELECT p FROM Panier p " +
            "LEFT JOIN FETCH p.contenuPaniers cp " +
            "LEFT JOIN FETCH cp.offre o " +
            "LEFT JOIN FETCH o.discipline d " + // La discipline est associée à l'Offre
            "WHERE p.utilisateur.idUtilisateur = :utilisateurId AND p.statut = :statut")
    Optional<Panier> findByUtilisateurIdAndStatutWithDetails(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("statut") StatutPanier statut
    );

    /**
     * Trouve un panier par l'ID de l'utilisateur et le statut du panier, sans charger les détails en profondeur.
     * @param utilisateurId ID de l'utilisateur.
     * @param statut Statut du panier.
     * @return Un Optional de Panier.
     */
    Optional<Panier> findByUtilisateur_idUtilisateurAndStatut(UUID utilisateurId, StatutPanier statut);

    /**
     * Trouve tous les paniers (avec leurs détails) qui contiennent une offre spécifique et ont un statut EN_ATTENTE.
     * @param offreId L'ID de l'offre.
     * @return Une liste de paniers contenant l'offre spécifiée.
     */
    @Query("SELECT DISTINCT p FROM Panier p " +
            "LEFT JOIN FETCH p.contenuPaniers cp " +
            "LEFT JOIN FETCH cp.offre o " +
            "LEFT JOIN FETCH o.discipline d " + // Fetch discipline si nécessaire pour le recalcul ou autre logique
            "WHERE o.idOffre = :offreId AND p.statut = fr.studi.bloc3jo2024.entity.enums.StatutPanier.EN_ATTENTE")
    List<Panier> findPaniersContenantOffreWithDetails(@Param("offreId") Long offreId);
}
