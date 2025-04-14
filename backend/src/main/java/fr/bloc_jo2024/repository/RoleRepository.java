package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByTypeRole(String typeRole);
}