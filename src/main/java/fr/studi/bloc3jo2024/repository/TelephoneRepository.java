package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Telephone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelephoneRepository extends JpaRepository<Telephone, Long> {

    /**
     * Récupère tous les numéros de téléphone d'un utilisateur donné.
     * permet d'envoyer un SMS de validation à l'utilisateur.
     * @param utilisateur L'UUID de l'utilisateur.
     * @return Une liste de téléphones appartenant à cet utilisateur.
     */
    List<Telephone> findByUtilisateur_IdUtilisateur(UUID utilisateur);

    /**
     * Récupère un numéro de téléphone spécifique.
     * @param numeroTelephone Le numéro de téléphone à rechercher.
     * @return Un Optional contenant le téléphone correspondant au numéro, ou un Optional vide si non trouvé.
     */
    Optional<Telephone> findByNumeroTelephone(String numeroTelephone);
}