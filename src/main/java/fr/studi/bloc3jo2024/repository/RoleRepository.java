package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTypeRole(TypeRole typeRole);
}
