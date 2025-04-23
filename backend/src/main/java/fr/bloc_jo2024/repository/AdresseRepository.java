package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Adresse;
import fr.bloc_jo2024.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {

    // Recherche par ville (pour l'évenements)
    List<Adresse> findByVille(String ville);

    //Recherche par ID de pays (pour l'évenements)
    List<Adresse> findByPays_Nom(String nomPays);

    // Recherche par adresse complète (pour l'offre)
    Optional<Adresse> findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
            int numeroRue, String nomRue, String ville, String codePostale, Pays pays
    );

    // Récupérer les adresses des utilisateurs
    @Query("SELECT a FROM Adresse a JOIN a.utilisateurs u WHERE u.idUtilisateur = :idUtilisateur")
    List<Adresse> findByUtilisateurs_IdUtilisateur(@Param("idUtilisateur") UUID idUtilisateur);

    // Récupérer l'adresse d'un événement
    @Query("SELECT a FROM Adresse a JOIN a.evenements e WHERE e.idEvenement = :idEvenement")
    Optional<Adresse> findByEvenement_IdEvenement(@Param("idEvenement") Long idEvenement);

    // Vérifie si une adresse est liée à un événement
    @Query("SELECT COUNT(a) > 0 FROM Adresse a JOIN a.evenements e WHERE a.idAdresse = :idAdresse")
    boolean adresseEstLieeAUnEvenement(@Param("idAdresse") Long idAdresse);
}