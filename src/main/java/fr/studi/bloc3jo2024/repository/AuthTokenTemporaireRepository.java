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
     * Recherche un token temporaire par sa valeur hashée.
     *
     * @param tokenHache La valeur hashée du token temporaire.
     * @return Un Optional contenant le token temporaire correspondant s'il est trouvé, sinon un Optional vide.
     */
    Optional<AuthTokenTemporaire> findByTokenHache(String tokenHache);

    /**
     * Recherche un token temporaire associé à un utilisateur spécifique et d'un type donné.
     *
     * @param utilisateur L'utilisateur auquel le token temporaire est associé.
     * @param typeToken   Le type du token temporaire (mot de passe oublié, activation de compte, jeton connexion).
     * @return Un Optional contenant le token temporaire correspondant s'il est trouvé, sinon un Optional vide.
     */
    Optional<AuthTokenTemporaire> findByUtilisateurAndTypeToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken);

    /**
     * Supprime tous les tokens temporaires dont la date d'expiration est antérieure à la date spécifiée.
     *
     * @param dateExpiration La date limite d'expiration. Tous les tokens expirés avant cette date seront supprimés.
     * @return Le nombre de tokens temporaires qui ont été supprimés.
     * Permet la maintenance et la sécurité, en supprimant régulièrement les tokens obsolètes.
     */
    long deleteByDateExpirationBefore(LocalDateTime dateExpiration);
}