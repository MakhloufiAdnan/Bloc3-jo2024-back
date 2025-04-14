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

    List<Adresse> findByVille(String ville);

    List<Adresse> findByPays_Nom(String nomPays);

    List<Adresse> findByPays_IdPays(Long idPays);

    Optional<Adresse> findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
            int numeroRue, String nomRue, String ville, String codePostal, Pays pays
    );

    // Récupérer les adresses des utilisateurs (moins courant ici)
    @Query("SELECT a FROM Adresse a JOIN a.utilisateurs u WHERE u.idUtilisateur = :idUtilisateur")
    List<Adresse> findByUtilisateurs_IdUtilisateur(@Param("idUtilisateur") UUID idUtilisateur);

    // Récupérer l'adresse d'un événement
    @Query("SELECT a FROM Adresse a JOIN a.evenements e WHERE e.idEvenement = :idEvenement")
    Optional<Adresse> findByEvenements_IdEvenement(@Param("idEvenement") Long idEvenement);
}