package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Recherche d'un rôle par son type (ADMIN ou USER)
    Optional<Role> findByTypeRole(String typeRole);

}