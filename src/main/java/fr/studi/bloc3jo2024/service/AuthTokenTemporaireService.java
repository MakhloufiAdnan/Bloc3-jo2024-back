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

@Service
@RequiredArgsConstructor
public class AuthTokenTemporaireService {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenTemporaireService.class);
    private static final int TOKEN_LOG_PREFIX_LENGTH = 8;

    private final AuthTokenTemporaireRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken, Duration validity) {
        if (utilisateur == null) {
            logger.error("Tentative de création de token pour un utilisateur null. Type de token: {}", typeToken);
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null pour la création du token.");
        }

        tokenRepository.findByUtilisateurAndTypeToken(utilisateur, typeToken)
                .filter(existingToken -> !existingToken.isUsed() && existingToken.getDateExpiration().isAfter(LocalDateTime.now()))
                .ifPresent(existingToken -> {
                    String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
                    logger.debug("Suppression du token actif existant ID {} de type {} pour l'utilisateur {}",
                            existingToken.getIdTokenTemp(), typeToken, userEmailForLog);
                    tokenRepository.delete(existingToken);
                });

        String rawTokenIdentifier = UUID.randomUUID().toString();
        String hashedTokenSecret = passwordEncoder.encode(rawTokenIdentifier);

        AuthTokenTemporaire entity = AuthTokenTemporaire.builder()
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(hashedTokenSecret)
                .typeToken(typeToken)
                .dateExpiration(LocalDateTime.now().plus(validity))
                .utilisateur(utilisateur)
                .isUsed(false)
                .build();

        tokenRepository.save(entity);
        String userEmailForLog = utilisateur.getEmail() != null ? utilisateur.getEmail() : "[email non disponible]";
        logger.info("Token temporaire de type {} (identifier: {}...) créé pour l'utilisateur {}",
                typeToken, rawTokenIdentifier.substring(0, Math.min(rawTokenIdentifier.length(), TOKEN_LOG_PREFIX_LENGTH)), userEmailForLog);
        return rawTokenIdentifier;
    }

    @Transactional(readOnly = true)
    public AuthTokenTemporaire validateToken(String rawTokenIdentifier, TypeAuthTokenTemp expectedType) {
        if (rawTokenIdentifier == null || rawTokenIdentifier.trim().isEmpty()) {
            logger.warn("Tentative de validation avec un token identifier brut vide ou null. Type attendu: {}", expectedType);
            throw new IllegalArgumentException("Le token brut fourni ne peut pas être vide ou null.");
        }

        String tokenForLog = rawTokenIdentifier.substring(0, Math.min(rawTokenIdentifier.length(), TOKEN_LOG_PREFIX_LENGTH)) + "...";

        Optional<AuthTokenTemporaire> tokenOpt = tokenRepository.findByTokenIdentifier(rawTokenIdentifier);

        if (tokenOpt.isEmpty()) {
            logger.warn("Token non trouvé en base pour l'identifier: {}. Type attendu: {}", tokenForLog, expectedType);
            throw new IllegalArgumentException("Token non trouvé.");
        }

        AuthTokenTemporaire token = tokenOpt.get();

        if (!passwordEncoder.matches(rawTokenIdentifier, token.getTokenHache())) {
            logger.error("DISCORDANCE CRITIQUE DE SÉCURITÉ: Le rawTokenIdentifier {} (ID Entité: {}) a été trouvé, " +
                            "mais passwordEncoder.matches() a échoué avec son tokenHache associé. ",
                    tokenForLog, token.getIdTokenTemp());
            throw new IllegalArgumentException("Incohérence de sécurité du token. Vérification du secret échouée.");
        }

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

    @Transactional
    public void markAsUsed(AuthTokenTemporaire token) {
        if (token == null || token.getIdTokenTemp() == null) {
            logger.error("Tentative de marquer comme utilisé un token null ou avec un ID null.");
            throw new IllegalArgumentException("Le token fourni pour le marquage ne peut pas être null ou manquer d'ID.");
        }

        AuthTokenTemporaire tokenToUpdate = tokenRepository.findById(token.getIdTokenTemp())
                .orElseThrow(() -> {
                    logger.error("Tentative de marquer comme utilisé un token (ID: {}) non trouvé en base.", token.getIdTokenTemp());
                    return new IllegalArgumentException("Token non trouvé en base pour marquage: " + token.getIdTokenTemp());
                });

        if (tokenToUpdate.isUsed()) {
            logger.warn("Tentative de marquer comme utilisé un token (ID: {}) qui l'est déjà. Aucune action supplémentaire.", tokenToUpdate.getIdTokenTemp());
            return;
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