package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    // Tous les événements à venir
    List<Evenement> findByDateEvenementAfter(LocalDateTime now);

    // Tous les événements dans une ville donnée
    @Query("SELECT e FROM Evenement e WHERE e.adresse.ville = :ville")
    List<Evenement> findEvenementsByVille(@Param("ville") String ville);

    // Tous les événements d'une épreuve donnée
    @Query("SELECT e FROM Evenement e JOIN e.comporters c WHERE c.epreuve.idEpreuve = :idEpreuve")
    List<Evenement> findEvenementsByEpreuveId(@Param("idEpreuve") Long idEpreuve);

    // Récupérer tous les événements à une date donnée
    @Query("SELECT e FROM Evenement e WHERE e.dateEvenement = :dateEvenement")
    List<Evenement> findEvenementsByDateEvenement(@Param("dateEvenement") LocalDateTime dateEvenement);
}