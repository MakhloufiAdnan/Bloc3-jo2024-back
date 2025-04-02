package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // Recherche un utilisateur par email
    Utilisateur findByEmail(String email);

    // Vérifie si un utilisateur existe par email
    boolean existsByEmail(String email);

    // Recherche par rôle (par exemple, pour l'administration)
    Optional<Utilisateur> findByRole_TypeRole(String role);

    // Recherche par email et rôle
    Optional<Utilisateur> findByEmailAndRole_TypeRole(String email, String role);
}


