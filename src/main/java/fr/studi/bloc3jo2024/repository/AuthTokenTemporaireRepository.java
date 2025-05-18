package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.AuthTokenTemporaire;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenTemporaireRepository extends JpaRepository<AuthTokenTemporaire, UUID> {

    /**
     * Trouve un token d'authentification temporaire par sa valeur hachée.
     *
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

    /**
     * Trouve tous les tokens temporaires qui ne sont pas utilisés (champ 'isUsed' = false dans l'entité)
     * ET dont la date d'expiration est postérieure à la date fournie (donc non expirés).
     * Spring Data JPA déduira la requête à partir de ce nom de méthode.
     * Le nom 'IsUsedFalse' correspond à une propriété 'isUsed' dans l'entité AuthTokenTemporaire.
     *
     * @param date La date actuelle, pour comparer avec la date d'expiration des tokens.
     * @return Une liste d'entités AuthTokenTemporaire actives.
     */
    // NOM DE MÉTHODE CORRIGÉ : Doit correspondre au nom de la propriété 'isUsed' dans l'entité.
    List<AuthTokenTemporaire> findAllByIsUsedFalseAndDateExpirationAfter(LocalDateTime date);
}