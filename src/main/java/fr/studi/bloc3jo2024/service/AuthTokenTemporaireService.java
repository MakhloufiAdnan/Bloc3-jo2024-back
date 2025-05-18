package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.AuthTokenTemporaire;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.repository.AuthTokenTemporaireRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service gérant la logique métier pour les tokens temporaires (validation de compte, réinitialisation de mot de passe).
 * Assure la création, la validation, l'utilisation et la purge des tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthTokenTemporaireService {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenTemporaireService.class);
    private static final int TOKEN_LOG_PREFIX_LENGTH = 8; // Pour tronquer les tokens dans les logs

    private final AuthTokenTemporaireRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouveau token temporaire pour un utilisateur.
     * Le token brut (identifiant) est retourné pour être envoyé à l'utilisateur, tandis qu'une version hachée
     * est stockée en base de données pour vérification.
     * Si un token du même type existe déjà pour cet utilisateur et n'est pas expiré/utilisé, il est d'abord supprimé.
     *
     * @param utilisateur L'utilisateur pour lequel le token est créé. Ne doit pas être null.
     * @param typeToken   Le type de token (ex: RESET_PASSWORD, VALIDATION_EMAIL).
     * @param validity    La durée de validité du token.
     * @return Le token brut (non haché, servant d'identifiant), à envoyer à l'utilisateur.
     * @throws IllegalArgumentException si l'utilisateur est null.
     */
    @Transactional
    public String createToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken, Duration validity) {
        if (utilisateur == null) {
            logger.error("Tentative de création de token pour un utilisateur null. Type de token: {}", typeToken);
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null pour la création du token.");
        }

        // pour éviter qu'un utilisateur ait plusieurs tokens valides pour la même action.
        tokenRepository.findByUtilisateurAndTypeToken(utilisateur, typeToken)
                .filter(existingToken -> !existingToken.isUsed() && existingToken.getDateExpiration().isAfter(LocalDateTime.now()))
                .ifPresent(existingToken -> {
                    String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
                    logger.debug("Suppression du token actif existant ID {} de type {} pour l'utilisateur {}",
                            existingToken.getIdTokenTemp(), typeToken, userEmailForLog);
                    tokenRepository.delete(existingToken);
                });

        String rawTokenIdentifier = UUID.randomUUID().toString(); // C'est le token_identifier
        String hashedTokenSecret = passwordEncoder.encode(rawTokenIdentifier); // C'est le token_hache

        AuthTokenTemporaire entity = AuthTokenTemporaire.builder()
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(hashedTokenSecret)
                .typeToken(typeToken)
                .dateExpiration(LocalDateTime.now().plus(validity))
                .utilisateur(utilisateur)
                .isUsed(false) // Par défaut grâce à @Builder.Default dans l'entité
                .build();

        tokenRepository.save(entity);
        String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
        logger.info("Token temporaire de type {} (identifier: {}...) créé pour l'utilisateur {}",
                typeToken, rawTokenIdentifier.substring(0, Math.min(rawTokenIdentifier.length(), TOKEN_LOG_PREFIX_LENGTH)), userEmailForLog);
        return rawTokenIdentifier; // On renvoie l'identifiant brut à l'utilisateur
    }

    /**
     * Valide un token brut (identifiant) fourni par un utilisateur.
     * Recherche le token par son identifiant, puis vérifie la correspondance du hachage,
     * son statut (non utilisé), sa date d'expiration, et son type.
     *
     * @param rawTokenIdentifier Le token brut (servant d'identifiant) à valider.
     * @param expectedType       Le type de token attendu pour cette opération de validation.
     * @return L'entité AuthTokenTemporaire si le token est valide.
     * @throws IllegalArgumentException Si le token n'est pas trouvé, si la vérification du secret (hachage) échoue,
     * s'il est déjà utilisé, ou s'il a expiré.
     * @throws IllegalStateException    Si le type du token trouvé ne correspond pas au type attendu.
     */
    @Transactional(readOnly = true)
    public AuthTokenTemporaire validateToken(String rawTokenIdentifier, TypeAuthTokenTemp expectedType) {
        if (rawTokenIdentifier == null || rawTokenIdentifier.trim().isEmpty()) {
            logger.warn("Tentative de validation avec un token identifier brut vide ou null. Type attendu: {}", expectedType);
            throw new IllegalArgumentException("Le token brut fourni ne peut pas être vide ou null.");
        }

        String tokenForLog = rawTokenIdentifier.substring(0, Math.min(rawTokenIdentifier.length(), TOKEN_LOG_PREFIX_LENGTH)) + "...";

        // Étape 1: Recherche directe du token par son identifiant brut (rawTokenIdentifier)
        Optional<AuthTokenTemporaire> tokenOpt = tokenRepository.findByTokenIdentifier(rawTokenIdentifier);

        if (tokenOpt.isEmpty()) {
            logger.warn("Token non trouvé en base pour l'identifier: {}. Type attendu: {}", tokenForLog, expectedType);
            throw new IllegalArgumentException("Token non trouvé.");
        }

        AuthTokenTemporaire token = tokenOpt.get();

        // Étape 2: Vérifier que le rawTokenIdentifier correspond bien au tokenHache stocké pour CETTE entité.
        // Cela garantit que même si quelqu'un connaissait/devinait un token_identifier valide,
        // il doit aussi implicitement "prouver" qu'il connaît le secret original qui a produit ce tokenHache.
        if (!passwordEncoder.matches(rawTokenIdentifier, token.getTokenHache())) {
            // Ce cas est critique et indique une possible corruption ou une erreur de logique majeure
            // si tokenIdentifier est censé être la source du tokenHache.
            logger.error("DISCORDANCE CRITIQUE DE SÉCURITÉ: Le rawTokenIdentifier {} (ID Entité: {}) a été trouvé, " +
                            "mais passwordEncoder.matches() a échoué avec son tokenHache associé. " +
                            "Cela ne devrait jamais arriver si la création du token est correcte.",
                    tokenForLog, token.getIdTokenTemp());
            throw new IllegalArgumentException("Incohérence de sécurité du token. Vérification du secret échouée.");
        }

        // Étape 3: Vérifications métier
        if (token.isUsed()) {
            logger.warn("Token ID {} (identifier: {}) déjà utilisé. Type attendu: {}", token.getIdTokenTemp(), tokenForLog, expectedType);
            throw new IllegalArgumentException("Token déjà utilisé.");
        }
        if (token.getDateExpiration().isBefore(LocalDateTime.now())) {
            logger.warn("Token ID {} (identifier: {}) expiré depuis {}. Type attendu: {}",
                    token.getIdTokenTemp(), tokenForLog, token.getDateExpiration(), expectedType);
            throw new IllegalArgumentException("Token expiré.");
        }
        if (token.getTypeToken() != expectedType) {
            logger.warn("Type de token incorrect pour ID {} (identifier: {}). Attendu: {}, Obtenu: {}.",
                    token.getIdTokenTemp(), tokenForLog, expectedType, token.getTypeToken());
            throw new IllegalStateException("Type de token incorrect. Attendu: " + expectedType + ", Obtenu: " + token.getTypeToken());
        }

        Utilisateur utilisateur = token.getUtilisateur();
        String userEmailForLog = (utilisateur != null && utilisateur.getEmail() != null) ?
                utilisateur.getEmail() : "[Utilisateur ou email non disponible]";
        logger.debug("Token validé avec succès: id {}, type {}, identifier {}, utilisateur {}",
                token.getIdTokenTemp(), token.getTypeToken(), tokenForLog, userEmailForLog);
        return token;
    }

    /**
     * Marque un token comme utilisé.
     * L'entité token doit être récupérée de la base de données dans cette transaction pour s'assurer
     * qu'elle est managée par JPA avant la modification.
     *
     * @param token L'entité AuthTokenTemporaire (obtenue après validation) à marquer comme utilisée.
     * @throws IllegalArgumentException si le token fourni est null ou n'est pas trouvé en base.
     */
    @Transactional
    public void markAsUsed(AuthTokenTemporaire token) {
        if (token == null || token.getIdTokenTemp() == null) {
            logger.error("Tentative de marquer comme utilisé un token null ou avec un ID null.");
            throw new IllegalArgumentException("Le token fourni pour le marquage ne peut pas être null ou manquer d'ID.");
        }

        // Récupérer l'entité managée pour s'assurer qu'elle est dans le contexte de persistance actuel
        AuthTokenTemporaire tokenToUpdate = tokenRepository.findById(token.getIdTokenTemp())
                .orElseThrow(() -> {
                    logger.error("Tentative de marquer comme utilisé un token (ID: {}) non trouvé en base.", token.getIdTokenTemp());
                    return new IllegalArgumentException("Token non trouvé en base pour marquage: " + token.getIdTokenTemp());
                });

        if (tokenToUpdate.isUsed()) {
            logger.warn("Tentative de marquer comme utilisé un token (ID: {}) qui l'est déjà.", tokenToUpdate.getIdTokenTemp());
        }

        tokenToUpdate.setUsed(true);
        tokenRepository.save(tokenToUpdate);

        String userEmailForLog = "[ID utilisateur inconnu]";
        if (tokenToUpdate.getUtilisateur() != null) {
            userEmailForLog = (tokenToUpdate.getUtilisateur().getEmail() != null) ?
                    tokenToUpdate.getUtilisateur().getEmail() :
                    "[Email non défini pour utilisateur " + tokenToUpdate.getUtilisateur().getIdUtilisateur() + "]";
        }
        logger.info("Token ID {} (identifier: {}...) marqué comme utilisé pour l'utilisateur {}",
                tokenToUpdate.getIdTokenTemp(),
                (tokenToUpdate.getTokenIdentifier() != null ? tokenToUpdate.getTokenIdentifier().substring(0, Math.min(tokenToUpdate.getTokenIdentifier().length(), TOKEN_LOG_PREFIX_LENGTH)) : "N/A"),
                userEmailForLog);
    }

    /**
     * Supprime tous les tokens temporaires qui ont expiré de la base de données.
     * Cette opération est effectuée directement en base de données pour plus d'efficacité.
     *
     * @return Le nombre de tokens purgés.
     */
    @Transactional
    public long purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        long count = tokenRepository.deleteByDateExpirationBefore(now);
        if (count > 0) {
            logger.info("{} tokens expirés avant {} ont été purgés.", count, now);
        } else {
            logger.info("Aucun token expiré à purger avant {}.", now);
        }
        return count;
    }
}