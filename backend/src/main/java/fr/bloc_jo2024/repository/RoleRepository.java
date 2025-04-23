package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Role;
import fr.bloc_jo2024.entity.enums.TypeRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTypeRole(TypeRole typeRole);
}