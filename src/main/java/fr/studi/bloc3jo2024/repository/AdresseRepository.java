package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Evenement;
import fr.studi.bloc3jo2024.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {

    /**
     * Recherche les adresses par ville.
     * Utilisé pour la recherche d'adresses potentielles pour des événements.
     * @param ville Le nom de la ville.
     * @return Une liste d'adresses situées dans la ville spécifiée.
     */
    List<Adresse> findByVille(String ville);

    /**
     * Recherche les adresses associées à des pays dont le nom correspond.
     * Utilisé pour filtrer les adresses d'événements par pays.
     * @param nomPays Le nom du pays.
     * @return Une liste d'adresses situées dans le pays spécifié.
     */
    List<Adresse> findByPays_NomPays(String nomPays);

    /**
     * Recherche une adresse complète en fonction de tous ses attributs.
     * Utilisé pour vérifier si une adresse existe déjà, notamment lors de la création d'une offre.
     * @param numeroRue Le numéro de la rue.
     * @param nomRue Le nom de la rue.
     * @param ville La ville.
     * @param codePostal Le code postal.
     * @param pays L'objet Pays associé à l'adresse.
     * @return Un Optional contenant l'adresse si elle existe, sinon un Optional vide.
     */
    Optional<Adresse> findByNumeroRueAndNomRueAndVilleAndCodePostalAndPays(
            int numeroRue, String nomRue, String ville, String codePostal, Pays pays
    );

    /**
     * Récupère les adresses associées à un utilisateur spécifique.
     * @param idUtilisateur L'UUID de l'utilisateur.
     * @return Une liste des adresses de l'utilisateur.
     */
    List<Adresse> findByUtilisateurs_IdUtilisateur(UUID idUtilisateur);

    /**
     * Récupère l'adresse associée à un événement spécifique.
     * Étant donné la relation Many-to-One de Evenement vers Adresse,
     * nous recherchons les adresses où la collection 'evenements' contient l'événement spécifié.
     * @param evenement L'objet Evenement.
     * @return L'adresse de l'événement, ou un Optional vide si non trouvée.
     */
    @Query("SELECT a FROM Adresse a JOIN a.evenements e WHERE e = :evenement")
    Optional<Adresse> findByEvenements(@Param("evenement") Evenement evenement);

    /**
     * Récupère la liste des adresses associées à un événement spécifique.
     * Utilisé pour récupérer toutes les adresses liées à un événement (bien que dans le modèle actuel,
     * un événement a une seule adresse).
     * @param evenement L'objet Evenement.
     * @return La liste des adresses liées à l'événement.
     */
    List<Adresse> findByEvenementsContaining(Evenement evenement);

    /**
     * Vérifie si une adresse est liée à au moins un événement.
     * Utilisé avant de supprimer une adresse pour éviter de supprimer une adresse activement utilisée.
     * @param idAdresse L'ID de l'adresse à vérifier.
     * @return true si l'adresse est liée à un événement, false sinon.
     */
    @Query("SELECT COUNT(a) > 0 FROM Adresse a JOIN a.evenements e WHERE a.idAdresse = :idAdresse")
    boolean isAdresseLieeAUnEvenement(@Param("idAdresse") Long idAdresse);

    /**
     * Recherche les adresses liées à des événements spécifiques et filtrées par l'ID du pays de l'adresse.
     * @param evenement L'objet Evenement pour lequel rechercher les adresses.
     * @param idPays L'ID du pays des adresses à rechercher.
     * @return Une liste d'adresses liées à l'événement et appartenant au pays spécifié.
     */
    @Query("SELECT a FROM Adresse a JOIN a.evenements e WHERE e = :evenement AND a.pays.idPays = :idPays")
    List<Adresse> findByEvenementsAndPays_IdPays(@Param("evenement") Evenement evenement, @Param("idPays") Long idPays);
}