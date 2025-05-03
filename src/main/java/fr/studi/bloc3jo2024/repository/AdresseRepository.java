package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Adresse;
import fr.studi.bloc3jo2024.entity.Discipline;
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
     * Utilisé pour la recherche d'adresses potentielles pour des disciplines.
     * @param ville Le nom de la ville.
     * @return Une liste d'adresses situées dans la ville spécifiée.
     */
    List<Adresse> findByVille(String ville);

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
     * Récupère l'adresse associée à une discipline spécifique.
     * Étant donné la relation Many-to-One de Discipline vers Adresse,
     * nous recherchons les adresses où la collection 'disciplines' contient la discipline spécifié.
     * @param discipline L'objet Discipline.
     * @return L'adresse de la discipline, ou un Optional vide si non trouvée.
     */
    @Query("SELECT a FROM Adresse a JOIN a.disciplines e WHERE e = :discipline")
    Optional<Adresse> findByDisciplines(@Param("discipline") Discipline discipline);

    /**
     * Récupère la liste des adresses associées à une discipline spécifique.
     * Utilisé pour récupérer toutes les adresses liées à une discipline particulière.
     * @param discipline L'objet Discipline.
     * @return La liste des adresses liées à la discipline.
     */
    List<Adresse> findByDisciplinesContaining(Discipline discipline);

    /**
     * Vérifie si une adresse est liée à au moins une discipline.
     * Utilisé avant de supprimer une adresse pour éviter de supprimer une adresse utilisée.
     * @param idAdresse L'ID de l'adresse à vérifier.
     * @return true si l'adresse est liée à une discipline, false sinon.
     */
    @Query("SELECT COUNT(a) > 0 FROM Adresse a JOIN a.disciplines e WHERE a.idAdresse = :idAdresse")
    boolean isAdresseLieeAUnDiscipline(@Param("idAdresse") Long idAdresse);

    /**
     * Recherche les adresses liées à des disciplines spécifiques et filtrées par l'ID du pays de l'adresse.
     * @param discipline L'objet Discipline pour lequel rechercher les adresses.
     * @param idPays L'ID du pays des adresses à rechercher.
     * @return Une liste d'adresses liées à la discipline et appartenant au pays spécifié.
     */
    @Query("SELECT a FROM Adresse a JOIN a.disciplines e WHERE e = :discipline AND a.pays.idPays = :idPays")
    List<Adresse> findByDisciplinesAndPays_IdPays(@Param("discipline") Discipline discipline, @Param("idPays") Long idPays);
}