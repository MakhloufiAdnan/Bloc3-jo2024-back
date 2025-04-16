package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.AuthTokenTempEnum;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_token_temp", columnDefinition = "UUID")
    private UUID idTokenTemp;

    @Column(name = "token_hache", nullable = false, unique = true, length = 255)
    private String tokenHache;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_token", nullable = false)
    private AuthTokenTempEnum typeToken;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Relation vers l'utilisateur concerné par ce token temporaire.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_token"))
    private Utilisateur utilisateur;
}