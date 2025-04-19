package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.AuthTokenTemporaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Recherche par token haché
public interface AuthTokenTemporaireRepository extends JpaRepository<AuthTokenTemporaire, UUID> {
    Optional<AuthTokenTemporaire> findByTokenHache(String tokenHache);
}