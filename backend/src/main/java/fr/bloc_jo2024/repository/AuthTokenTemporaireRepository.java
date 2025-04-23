package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.AuthTokenTemporaire;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.enums.TypeAuthTokenTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AuthTokenTemporaireRepository extends JpaRepository<AuthTokenTemporaire, UUID> {
    Optional<AuthTokenTemporaire> findByTokenHache(String tokenHache);
    Optional<AuthTokenTemporaire> findByUtilisateurAndTypeToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken);
    int deleteByDateExpirationBefore(LocalDateTime dateExpiration);
}