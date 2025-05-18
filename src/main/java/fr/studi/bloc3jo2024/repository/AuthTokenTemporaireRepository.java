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

    /**
     * Trouve un token d'authentification temporaire par sa valeur d'identification brute (non hachée).
     *
     * @param tokenIdentifier La chaîne du token brut (UUID).
     * @return Un Optional contenant le token s'il est trouvé, sinon vide.
     */
    Optional<AuthTokenTemporaire> findByTokenIdentifier(String tokenIdentifier);

    /**
     * Trouve un token d'authentification temporaire par sa valeur hachée.
     * (Peut-être moins utile maintenant, mais on la garde au cas où)
     * @param tokenHache La chaîne du token haché.
     * @return Un Optional contenant le token s'il est trouvé, sinon vide.
     */
    Optional<AuthTokenTemporaire> findByTokenHache(String tokenHache);

    /**
     * Trouve un token d'authentification temporaire pour un utilisateur spécifique et un type de token.
     * Utile pour s'assurer qu'un utilisateur n'a pas plusieurs tokens actifs du même type.
     *
     * @param utilisateur L'utilisateur associé au token.
     * @param typeToken   Le type du token.
     * @return Un Optional contenant le token s'il est trouvé, sinon vide.
     */
    Optional<AuthTokenTemporaire> findByUtilisateurAndTypeToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken);

    /**
     * Supprime tous les tokens d'authentification temporaires dont la date d'expiration est antérieure à la date donnée.
     *
     * @param dateExpiration La date et l'heure limites pour l'expiration.
     * @return Le nombre de tokens supprimés.
     */
    long deleteByDateExpirationBefore(LocalDateTime dateExpiration);
}