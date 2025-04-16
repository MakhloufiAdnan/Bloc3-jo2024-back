package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "authentifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_token", columnDefinition = "UUID")
    private UUID idToken;

    // Token complémentaire (pour vérification)
    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    // Date d'expiration du token
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Mot de passe encodé (donné par PasswordEncoder dans le service)
    @Column(name = "mot_passe", nullable = false, length = 255)
    private String motPasseHache;

    // Sel du mot de passe (pour le hachage)
    @Column(name = "salt", nullable = false, length = 255)
    private String salt;

    // Relation unidirectionnelle vers l'utilisateur
    @OneToOne
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "idUtilisateur", foreignKey = @ForeignKey(name = "fk_authentification_utilisateur"))
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