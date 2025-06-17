package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Billet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BilletRepository extends JpaRepository<Billet, Long> {

    Optional<Billet> findByCleFinaleBillet(String cleFinaleBillet);

    /**
     * Récupère toutes les clés finales des billets qui n'ont pas encore été scannés.
     * @return Une liste de chaînes de caractères représentant les clés des billets valides.
     */
    @Query("SELECT b.cleFinaleBillet FROM Billet b WHERE b.isScanned = false")
    List<String> findAllValidTicketKeys();
}