package fr.bloc_jo2024.entity;
import fr.bloc_jo2024.entity.Utilisateur;

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
public class Authentification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idToken;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime dateExpiration;

    @Column(nullable = false)
    private String motPasseHache;

    @Column(nullable = false)
    @Builder.Default
    private String salt = "defaultValue";

    @OneToOne
    @JoinColumn(name = "idUtilisateur", nullable = false, unique = true)
    private Utilisateur utilisateur;

    // @PrePersist va s'exécuter avant d'enregistrer l'entité dans la base de données
    @PrePersist
    public void hacherMotPasse() {
        if (this.salt == null) {
            this.salt = UUID.randomUUID().toString();
        }

        //BCryptPasswordEncoder hachera le mot de passe + le sel et le stockera
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.motPasseHache = encoder.encode(this.motPasseHache + this.salt);
    }

    // Vérifiez le mot de passe saisi avec le mot de passe haché et le sel stockés
    public boolean verifierMotPasse(String motPasseSaisi) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(motPasseSaisi + this.salt, this.motPasseHache);
    }
}

