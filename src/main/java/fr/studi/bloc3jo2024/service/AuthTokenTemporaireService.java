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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenTemporaireService {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenTemporaireService.class);

    private final AuthTokenTemporaireRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée un nouveau token temporaire pour un utilisateur.
     * Si un token du même type existe déjà pour cet utilisateur, il est supprimé avant la création du nouveau.
     *
     * @param utilisateur L'utilisateur pour lequel le token est créé.
     * @param typeToken   Le type de token (ex: RESET_PASSWORD, VALIDATION_EMAIL).
     * @param validity    La durée de validité du token.
     * @return Le token brut (non haché), à envoyer à l'utilisateur.
     */
    @Transactional
    public String createToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken, Duration validity) {
        if (utilisateur == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null pour la création du token.");
        }

        // Supprimer les tokens existants du même type pour cet utilisateur pour éviter les doublons
        tokenRepository.findByUtilisateurAndTypeToken(utilisateur, typeToken)
                .ifPresent(existingToken -> {
                    String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
                    logger.debug("Suppression du token existant de type {} pour l'utilisateur {}", typeToken, userEmailForLog);
                    tokenRepository.delete(existingToken);
                });

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(rawToken);

        AuthTokenTemporaire entity = AuthTokenTemporaire.builder()
                .tokenHache(hashedToken)
                .typeToken(typeToken)
                .dateExpiration(LocalDateTime.now().plus(validity))
                .utilisateur(utilisateur)
                .isUsed(false) // Par défaut à false grâce à @Builder.Default dans l'entité
                .build();

        tokenRepository.save(entity);
        String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
        logger.info("Token temporaire de type {} créé pour l'utilisateur {}", typeToken, userEmailForLog);
        return rawToken;
    }

    /**
     * Valide un token brut fourni par un utilisateur.
     * Vérifie son existence, son statut (non utilisé), sa date d'expiration, et son type.
     *
     * @param rawToken     Le token brut à valider.
     * @param expectedType Le type de token attendu pour cette opération de validation.
     * @return L'entité AuthTokenTemporaire si le token est valide.
     * @throws IllegalArgumentException Si le token n'est pas trouvé, est déjà utilisé, ou a expiré.
     * @throws IllegalStateException    Si le type du token trouvé ne correspond pas au type attendu.
     */
    @Transactional(readOnly = true)
    public AuthTokenTemporaire validateToken(String rawToken, TypeAuthTokenTemp expectedType) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Le token brut fourni ne peut pas être vide ou null.");
        }

        // Itérer sur tous les tokens peut être inefficace.
        // Si une optimisation est nécessaire, envisagez une requête plus ciblée si possible,
        // ou assurez-vous que la table des tokens n'est pas excessivement grande.
        // Pour cet exemple, nous parcourons tous les tokens pour trouver celui qui correspond
        // au hash du rawToken, afin de pouvoir ensuite vérifier son état et retourner des messages d'erreur spécifiques.
        List<AuthTokenTemporaire> allTokens = tokenRepository.findAll();
        Optional<AuthTokenTemporaire> matchedTokenOpt = Optional.empty();

        for (AuthTokenTemporaire token : allTokens) {
            if (passwordEncoder.matches(rawToken, token.getTokenHache())) {
                matchedTokenOpt = Optional.of(token);
                break; // On suppose qu'un rawToken ne correspond qu'à un seul tokenHache (unicité du tokenHache)
            }
        }

        if (matchedTokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token non trouvé.");
        }

        AuthTokenTemporaire token = matchedTokenOpt.get();

        if (token.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé.");
        }
        if (token.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré.");
        }
        if (token.getTypeToken() != expectedType) {
            throw new IllegalStateException("Type de token incorrect. Attendu: " + expectedType + ", Obtenu: " + token.getTypeToken());
        }

        Utilisateur utilisateur = token.getUtilisateur();
        String userEmailForLog = (utilisateur != null && utilisateur.getEmail() != null) ? utilisateur.getEmail() : "[Utilisateur ou email non disponible]";
        logger.debug("Token validé avec succès: id {}, type {}, utilisateur {}", token.getIdTokenTemp(), token.getTypeToken(), userEmailForLog);
        return token;
    }

    /**
     * Marque un token comme utilisé.
     *
     * @param token L'entité AuthTokenTemporaire à marquer.
     * @throws IllegalArgumentException si le token est null.
     */
    @Transactional
    public void markAsUsed(AuthTokenTemporaire token) {
        if (token == null) {
            throw new IllegalArgumentException("Le token fourni pour le marquage ne peut pas être null.");
        }
        // Vérifier si le token est déjà chargé et géré par le contexte de persistance
        AuthTokenTemporaire tokenToUpdate = tokenRepository.findById(token.getIdTokenTemp())
                .orElseThrow(() -> new IllegalArgumentException("Tentative de marquer comme utilisé un token non trouvé en base: " + token.getIdTokenTemp()));

        tokenToUpdate.setUsed(true);
        tokenRepository.save(tokenToUpdate); // Sauvegarde explicite après modification

        String userEmailForLog = "[ID utilisateur inconnu]";
        if (tokenToUpdate.getUtilisateur() != null) {
            userEmailForLog = (tokenToUpdate.getUtilisateur().getEmail() != null) ?
                    tokenToUpdate.getUtilisateur().getEmail() :
                    "[Email non défini pour utilisateur " + tokenToUpdate.getUtilisateur().getIdUtilisateur() + "]";
        }
        logger.info("Token {} marqué comme utilisé pour l'utilisateur {}", tokenToUpdate.getIdTokenTemp(), userEmailForLog);
    }

    /**
     * Supprime tous les tokens temporaires qui ont expiré.
     *
     * @return Le nombre de tokens purgés.
     */
    @Transactional
    public long purgeExpiredTokens() {
        long count = tokenRepository.deleteByDateExpirationBefore(LocalDateTime.now());
        if (count > 0) {
            logger.info("{} tokens expirés ont été purgés.", count);
        }
        return count;
    }
}