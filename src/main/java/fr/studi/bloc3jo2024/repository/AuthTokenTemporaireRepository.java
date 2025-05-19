package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.AuthTokenTemporaire;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenTemporaireRepository extends JpaRepository<AuthTokenTemporaire, UUID> {

    Optional<AuthTokenTemporaire> findByTokenIdentifier(String tokenIdentifier);

    Optional<AuthTokenTemporaire> findByTokenHache(String tokenHache);

    Optional<AuthTokenTemporaire> findByUtilisateurAndTypeToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken);

    long deleteByDateExpirationBefore(LocalDateTime dateExpiration);
}