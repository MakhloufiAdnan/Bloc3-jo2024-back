package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.AuthTokenTempEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthTokenTemporaire {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idTokenTemp;

    @Column(nullable = false, unique = true)
    private String tokenHache;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthTokenTempEnum typeToken;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_token"))
    private Utilisateur utilisateur;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void setToken(String token) {
        this.tokenHache = encoder.encode(token);
    }

    public boolean verifierToken(String tokenClair) {
        return encoder.matches(tokenClair, this.tokenHache) && LocalDateTime.now().isBefore(this.dateExpiration);
    }
}

