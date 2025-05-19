package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    /**
     * Recherche un utilisateur par son adresse e-mail.
     * @param email L'adresse e-mail de l'utilisateur.
     * @return Un Optional contenant l'utilisateur s'il est trouvé.
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie si un utilisateur existe avec l'adresse e-mail donnée.
     * @param email L'adresse e-mail à vérifier.
     * @return true si un utilisateur avec cet e-mail existe, false sinon.
     */
    boolean existsByEmail(String email);
}