package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // Recherche un utilisateur par email
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Recherche un utilisateur par son email et charge explicitement sa relation 'role'.
     * Ceci est utile pour éviter LazyInitializationException lorsque les rôles sont nécessaires
     * après que la session Hibernate initiale soit fermée.
     * @param email L'email de l'utilisateur à rechercher.
     * @return Un Optional contenant l'Utilisateur avec son rôle initialisé, ou Optional.empty() si non trouvé.
     */
    @Query("SELECT u FROM Utilisateur u JOIN FETCH u.role WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithRole(@Param("email") String email);

    // Vérifie si un utilisateur existe par email
    boolean existsByEmail(String email);
}
