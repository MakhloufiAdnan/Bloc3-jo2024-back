package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.UUID;

@Entity
@Table(name = "authentifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token_uuid", columnDefinition = "UUID")
    private UUID idToken;

    // Token principal d'authentification de l'utilisateur, généré lors de l'inscription (clé utilisateur).
    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    // Mot de passe encodé (donné par PasswordEncoder dans le service)
    @Column(name = "mot_passe_hache", nullable = false, length = 255)
    private String motPasseHache;

    // Relation One-to-One vers l'entité Utilisateur. Chaque utilisateur a une authentification associée.
    @OneToOne
    @JoinColumn(name = "id_utilisateur_uuid", referencedColumnName = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    // Instance de BCryptPasswordEncoder pour encoder et vérifier le mot de passe
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Encode le mot de passe en clair et le stocke dans motPasseHache.
    public void setMotPasse(String motPasseClair) {
        this.motPasseHache = encoder.encode(motPasseClair);
    }

    // Vérifie que le mot de passe en clair correspond au mot de passe haché stocké.
    public boolean verifierMotPasse(String motPasseClair) {
        return encoder.matches(motPasseClair, this.motPasseHache);
    }
}