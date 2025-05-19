package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Trouver un rôle par son type.
     *
     * @param typeRole le type de rôle
     * @return Optional contenant le rôle s'il est trouvé, ou vide s'il n'est pas trouvé
     */
    Optional<Role> findByTypeRole(TypeRole typeRole);
}
