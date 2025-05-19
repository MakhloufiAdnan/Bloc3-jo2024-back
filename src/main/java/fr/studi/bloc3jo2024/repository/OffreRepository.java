package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    /**
     * Trouve les offres par leur statut, avec pagination.
     * @param statutOffre Le statut de l'offre à rechercher.
     * @param pageable L'objet de pagination.
     * @return Une page d'offres correspondant au statut.
     */
    Page<Offre> findByStatutOffre(StatutOffre statutOffre, Pageable pageable);

    /**
     * Compte le nombre de billets (ou ventes associées) par ID d'offre.
     * Retourne l'ID de l'offre et le compte.
     * @return Une liste de tableaux d'objets, où chaque tableau contient [idOffre, COUNT(billets)].
     */
    @Query("SELECT o.idOffre, COUNT(b) FROM Offre o LEFT JOIN o.billets b GROUP BY o.idOffre")
    List<Object[]> countBilletsByOffreId();

    /**
     * Compte le nombre de billets (ou ventes associées) par type d'offre.
     * @return Une liste de tableaux d'objets, où chaque tableau contient [TypeOffre, COUNT(billets)].
     */
    @Query("SELECT o.typeOffre, COUNT(b) FROM Offre o LEFT JOIN o.billets b GROUP BY o.typeOffre")
    List<Object[]> countBilletsByTypeOffre();


    /**
     * Met à jour le statut des offres expirées en masse.
     * Passe les offres du statut DISPONIBLE à EXPIRE si leur date d'expiration est passée.
     * @param now La date et heure actuelles pour la comparaison.
     * @return Le nombre d'offres mises à jour.
     */
    @Modifying
    @Query("UPDATE Offre o SET o.statutOffre = fr.studi.bloc3jo2024.entity.enums.StatutOffre.EXPIRE " +
            "WHERE o.statutOffre = fr.studi.bloc3jo2024.entity.enums.StatutOffre.DISPONIBLE " +
            "AND o.dateExpiration IS NOT NULL AND o.dateExpiration < :now")
    int updateStatusForExpiredOffers(@Param("now") LocalDateTime now);
}