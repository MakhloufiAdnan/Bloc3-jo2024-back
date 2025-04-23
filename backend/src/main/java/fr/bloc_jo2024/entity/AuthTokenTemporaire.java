package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.TypeAuthTokenTemp;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens_temporaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokenTemporaire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token_temp_uuid", columnDefinition = "UUID")
    private UUID idTokenTemp;

    // Token temporaire lui-même (pour la connexion, la réinitialisation du mot de passe, la validation d'email).
    @Column(name = "token_hache", nullable = false, unique = true, length = 255)
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
    @ManyToOne
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;
}