package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.AuthTokenTempEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_tokens_temporaire")
public class AuthTokenTemporaire {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idTokenTemp", columnDefinition = "UUID")
    private UUID idTokenTemp;

    // Stocke le token après encodage
    @Column(nullable = false, unique = true, length = 255)
    private String tokenHache;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_token", nullable = false)
    private AuthTokenTempEnum typeToken;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Relation vers l'utilisateur concerné par ce token temporaire.
    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_token"))
    private Utilisateur utilisateur;

    // Instance de BCryptPasswordEncoder pour encoder et vérifier le token
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
        Encode le token en clair et le stocke dans tokenHache.
        @param tokenClair Le token en clair fourni par le service.
     */
    public void setToken(String tokenClair) {
        this.tokenHache = encoder.encode(tokenClair);
    }

    /**
     * Vérifie que le token en clair correspond au token stocké et que la date d'expiration n'est pas dépassée.
     * @param tokenClair Le token en clair à vérifier.
     * @return true si le token est correct et non expiré, false sinon.
     */
    public boolean verifierToken(String tokenClair) {
        return encoder.matches(tokenClair, this.tokenHache) &&
                LocalDateTime.now().isBefore(this.dateExpiration);
    }
}