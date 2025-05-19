package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Discipline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    @Query(value = "SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.dateDiscipline > :now",
            countQuery = "SELECT count(d) FROM Discipline d WHERE d.dateDiscipline > :now")
    Page<Discipline> findFutureDisciplinesWithAdresse(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE Discipline d SET d.nbPlaceDispo = d.nbPlaceDispo - :nb " +
            "WHERE d.idDiscipline = :id AND d.nbPlaceDispo >= :nb")
    int decrementerPlaces(@Param("id") Long id, @Param("nb") int nb);

    @Query(value = "SELECT d FROM Discipline d JOIN FETCH d.adresse a WHERE a.ville = :ville",
            countQuery = "SELECT count(d) FROM Discipline d JOIN d.adresse a WHERE a.ville = :ville")
    Page<Discipline> findDisciplinesByVilleWithAdresse(@Param("ville") String ville, Pageable pageable);

    @Query(value = "SELECT DISTINCT d FROM Discipline d JOIN FETCH d.adresse JOIN d.comporte c WHERE c.epreuve.idEpreuve = :idEpreuve",
            countQuery = "SELECT count(DISTINCT d) FROM Discipline d JOIN d.comporte c WHERE c.epreuve.idEpreuve = :idEpreuve")
    Page<Discipline> findDisciplinesByEpreuveIdWithAdresse(@Param("idEpreuve") Long idEpreuve, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Discipline d JOIN FETCH d.adresse adr JOIN d.comporte c WHERE c.epreuve.idEpreuve IN :epreuveIds")
    Set<Discipline> findDisciplinesByEpreuveIdsWithAdresse(@Param("epreuveIds") List<Long> epreuveIds);


    @Query(value = "SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.dateDiscipline = :dateDiscipline",
            countQuery = "SELECT count(d) FROM Discipline d WHERE d.dateDiscipline = :dateDiscipline")
    Page<Discipline> findDisciplinesByDateDisciplineWithAdresse(@Param("dateDiscipline") LocalDateTime dateDiscipline, Pageable pageable);

    @Query(value = "SELECT d FROM Discipline d JOIN FETCH d.adresse",
            countQuery = "SELECT count(d) FROM Discipline d")
    Page<Discipline> findAllWithAdresse(Pageable pageable);
}