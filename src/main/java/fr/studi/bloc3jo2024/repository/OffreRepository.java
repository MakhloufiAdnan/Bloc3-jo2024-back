package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    List<Offre> findByStatutOffre(StatutOffre statutOffre);

    @Query("SELECT o, COUNT(b) FROM Offre o LEFT JOIN o.billets b GROUP BY o")
    List<Object[]> countBilletsByOffre();

    @Query("SELECT o.typeOffre, COUNT(b) FROM Offre o LEFT JOIN o.billets b GROUP BY o.typeOffre")
    List<Object[]> countBilletsByTypeOffre();
}