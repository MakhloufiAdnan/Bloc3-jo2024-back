package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    // Disciplines futures
    List<Discipline> findByDateDisciplineAfter(LocalDateTime now);

    // Par ville
    @Query("SELECT d FROM Discipline d WHERE d.adresse.ville = :ville")
    List<Discipline> findDisciplinesByVille(@Param("ville") String ville);

    // Par épreuve
    @Query("SELECT d FROM Discipline d JOIN d.comporters c WHERE c.epreuve.idEpreuve = :idEpreuve")
    List<Discipline> findDisciplinesByEpreuveId(@Param("idEpreuve") Long idEpreuve);

    // Par date exacte
    @Query("SELECT d FROM Discipline d WHERE d.dateDiscipline = :dateDiscipline")
    List<Discipline> findDisciplinesByDateDiscipline(@Param("dateDiscipline") LocalDateTime dateDiscipline);
}