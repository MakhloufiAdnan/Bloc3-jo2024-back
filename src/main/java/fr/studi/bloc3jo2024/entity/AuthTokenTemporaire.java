package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens_temporaire", indexes = {
        @Index(name = "idx_authtokentemp_token_identifier", columnList = "token_identifier", unique = true),
        @Index(name = "idx_authtokentemp_token_hache", columnList = "token_hache", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokenTemporaire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token_temp_uuid", columnDefinition = "UUID")
    private UUID idTokenTemp;

    // Le token brut (UUID) qui est envoyé à l'utilisateur et qu'il renverra.
    // Utilisé pour une recherche rapide et ciblée en base de données.
    @Column(name = "token_identifier", nullable = false, unique = true, length = 36)
    private String tokenIdentifier;

    // Le token haché (secret), version hachée du token_identifier.
    // Utilisé pour vérifier que le token_identifier fourni est bien celui qui a été initialement généré.
    @Column(name = "token_hache", nullable = false, unique = true)
    private String tokenHache;

    // Type de token temporaire (CONNECTION, RESET_PASSWORD, VALIDATION_EMAIL).
    @Enumerated(EnumType.STRING)
    @Column(name = "type_token", nullable = false)
    private TypeAuthTokenTemp typeToken;

    // Date et heure d'expiration du token temporaire.
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Indique si le token a été utilisé ou non.
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    // Relation Many-to-One vers l'entité Utilisateur. Chaque token temporaire est associé à un utilisateur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;
}