package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.AuthTokenTemporaire;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.repository.AuthTokenTemporaireRepository;
import lombok.RequiredArgsConstructor;
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

    private final AuthTokenTemporaireRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée et persiste un token temporaire pour l'utilisateur donné et renvoie le token clair.
     * @param utilisateur l'utilisateur cible
     * @param typeToken   le type de token (RESET_PASSWORD, VALIDATION_EMAIL, etc.)
     * @param validity    durée de vie du token (ex: Duration.ofHours(1))
     * @return le token clair à envoyer par email
     */
    @Transactional
    public String createToken(Utilisateur utilisateur, TypeAuthTokenTemp typeToken, Duration validity) {
        // Avant de créer, on peut purger un éventuel ancien token de même type
        tokenRepository.findByUtilisateurAndTypeToken(utilisateur, typeToken)
                .ifPresent(tokenRepository::delete);

        // Génération d'un token aléatoire
        String rawToken = UUID.randomUUID().toString();
        String hashed  = passwordEncoder.encode(rawToken);

        AuthTokenTemporaire entity = AuthTokenTemporaire.builder()
                .tokenHache(hashed)
                .typeToken(typeToken)
                .dateExpiration(LocalDateTime.now().plus(validity))
                .utilisateur(utilisateur)
                .build();

        tokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Valide un token temporaire (vérifie existence, type, validité et non-utilisation).
     * @throws IllegalArgumentException si invalide ou expiré
     * @throws IllegalStateException    si déjà utilisé ou type incorrect
     */
    @Transactional(readOnly = true)
    public AuthTokenTemporaire validateToken(String rawToken, TypeAuthTokenTemp expectedType) {

        // Cherche tous les tokens du type non utilisés et non expirés
        LocalDateTime now = LocalDateTime.now();
        Optional<AuthTokenTemporaire> opt = tokenRepository.findAll().stream()
                .filter(t -> t.getTypeToken() == expectedType)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getDateExpiration().isAfter(now))
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHache()))
                .findFirst();

        AuthTokenTemporaire token = opt.orElseThrow(
                () -> new IllegalArgumentException("Token temporaire invalide ou expiré.")
        );

        // Vérifie le type
        if (token.getTypeToken() != expectedType) {
            throw new IllegalStateException("Type de token incorrect.");
        }
        return token;
    }

    /**
     * Marque un token comme utilisé.
     */
    @Transactional
    public void markAsUsed(AuthTokenTemporaire token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    /**
     * Supprime les tokens expirés (à appeler périodiquement).
     */
    @Transactional
    public long purgeExpiredTokens() {
        return tokenRepository.deleteByDateExpirationBefore(LocalDateTime.now());
    }
}
