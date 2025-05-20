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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    @Query("SELECT o FROM Offre o LEFT JOIN FETCH o.discipline d WHERE o.statutOffre = :statutOffre")
    Page<Offre> findByStatutOffreWithDiscipline(@Param("statutOffre") StatutOffre statutOffre, Pageable pageable);

    Page<Offre> findByStatutOffre(StatutOffre statutOffre, Pageable pageable);

    @Query("SELECT o.idOffre, COUNT(b.idBillet) FROM Offre o LEFT JOIN o.billets b GROUP BY o.idOffre")
    List<Object[]> countBilletsByOffreId();

    @Query("SELECT o.typeOffre, COUNT(b.idBillet) FROM Offre o LEFT JOIN o.billets b GROUP BY o.typeOffre")
    List<Object[]> countBilletsByTypeOffre();

    @Modifying
    @Query("UPDATE Offre o SET o.statutOffre = fr.studi.bloc3jo2024.entity.enums.StatutOffre.EXPIRE " +
            "WHERE o.statutOffre = fr.studi.bloc3jo2024.entity.enums.StatutOffre.DISPONIBLE " +
            "AND (" +
            "  (o.dateExpiration IS NOT NULL AND o.dateExpiration < :nowDateTime) " +
            "  OR " +
            "  (o.discipline IS NOT NULL AND CAST(o.discipline.dateDiscipline AS date) <= :currentDate)" +
            ")")
    int updateStatusForEffectivelyExpiredOffers(
            @Param("nowDateTime") LocalDateTime nowDateTime,
            @Param("currentDate") LocalDate currentDate
    );

    @Query(value = "SELECT o FROM Offre o LEFT JOIN FETCH o.discipline d",
            countQuery = "SELECT count(o) FROM Offre o")
    Page<Offre> findAllWithDiscipline(Pageable pageable);
}