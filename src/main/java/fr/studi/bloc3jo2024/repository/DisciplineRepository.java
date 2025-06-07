package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Discipline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    // Disciplines futures avec l'adresse (pour éviter N+1)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.dateDiscipline > :now ORDER BY d.dateDiscipline ASC")
    Page<Discipline> findFutureDisciplinesWithAdresse(@Param("now") LocalDateTime now, Pageable pageable);

    // Requête existante, peut être utilisée pour des cas simples sans fetch immédiat de l'adresse
    List<Discipline> findByDateDisciplineAfter(LocalDateTime now);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Discipline d SET d.nbPlaceDispo = d.nbPlaceDispo - :nb"
            + " WHERE d.idDiscipline = :id AND d.nbPlaceDispo >= :nb")
    int decrementerPlaces(@Param("id") Long id, @Param("nb") int nb);

    // Par ville avec l'adresse (pour éviter N+1)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.adresse.ville = :ville")
    List<Discipline> findDisciplinesByVille(@Param("ville") String ville);

    // Par ville avec l'adresse et pagination (pour les tests de repository)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.adresse.ville = :ville")
    Page<Discipline> findDisciplinesByVilleWithAdresse(@Param("ville") String ville, Pageable pageable);


    // Par épreuve avec l'adresse (pour éviter N+1)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse da JOIN d.comporters c WHERE c.epreuve.idEpreuve = :idEpreuve")
    List<Discipline> findDisciplinesByEpreuveId(@Param("idEpreuve") Long idEpreuve);

    // Par épreuve avec l'adresse et pagination (pour les tests de repository)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse da JOIN d.comporters c WHERE c.epreuve.idEpreuve = :idEpreuve")
    Page<Discipline> findDisciplinesByEpreuveIdWithAdresse(@Param("idEpreuve") Long idEpreuve, Pageable pageable);


    // Par date exacte avec l'adresse (pour éviter N+1)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse WHERE d.dateDiscipline = :dateDiscipline")
    List<Discipline> findDisciplinesByDateDiscipline(@Param("dateDiscipline") LocalDateTime dateDiscipline);


    // Trouver toutes les disciplines avec leur adresse (pour findAll filtré si pas de filtre)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse")
    List<Discipline> findAllWithAdresse(); // Version sans pagination pour le service

    // Trouver toutes les disciplines avec leur adresse et pagination (pour les tests de repository)
    @Query("SELECT d FROM Discipline d JOIN FETCH d.adresse")
    Page<Discipline> findAllWithAdresse(Pageable pageable);

    @Query("SELECT DISTINCT d FROM Discipline d JOIN FETCH d.adresse da JOIN d.comporters c WHERE c.epreuve.idEpreuve IN :epreuveIds")
    Set<Discipline> findDisciplinesByEpreuveIdsWithAdresse(@Param("epreuveIds") List<Long> epreuveIds);

}