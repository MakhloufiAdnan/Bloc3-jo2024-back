package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.ContenuPanier;
import fr.studi.bloc3jo2024.entity.ContenuPanierId;
import fr.studi.bloc3jo2024.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenuPanierRepository extends JpaRepository<ContenuPanier, ContenuPanierId> {

    /**
     * Supprime tous les {@link ContenuPanier} associés à un ID d'offre spécifique.
     * Cette opération est une suppression en masse et est plus efficace que de charger
     * puis supprimer chaque entité individuellement.
     *
     * @param offreId L'ID de l' {@link fr.studi.bloc3jo2024.entity.Offre} dont les contenus
     * doivent être supprimés des paniers.
     * @return Le nombre d'entrées {@link ContenuPanier} supprimées.
     */
    @Modifying
    @Query("DELETE FROM ContenuPanier cp WHERE cp.offre.idOffre = :offreId")
    int deleteByOffreId(@Param("offreId") Long offreId);

    /**
     * Supprime tous les {@link ContenuPanier} associés à un {@link Panier} spécifique.
     * Utile pour implémenter la fonctionnalité de vidage de panier.
     * C'est une opération de suppression en masse.
     *
     * @param panier Le {@link Panier} dont les contenus doivent être supprimés.
     * @return Le nombre d'entrées {@link ContenuPanier} supprimées.
     */
    @Modifying
    @Query("DELETE FROM ContenuPanier cp WHERE cp.panier = :panier")
    int deleteByPanier(@Param("panier") Panier panier);

}