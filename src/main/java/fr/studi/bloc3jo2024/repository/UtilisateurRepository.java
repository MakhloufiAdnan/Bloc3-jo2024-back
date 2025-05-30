package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // Recherche un utilisateur par email
    Optional<Utilisateur> findByEmail(String email);

    // Vérifie si un utilisateur existe par email
    boolean existsByEmail(String email);
}
