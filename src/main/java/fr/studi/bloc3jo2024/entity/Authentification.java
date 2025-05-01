package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    // Mot de passe encodé (donné par PasswordEncoder dans le service)
    @Column(name = "mot_passe_hache", nullable = false)
    private String motPasseHache;

    // Relation One-to-One vers l'entité Utilisateur. Chaque utilisateur a une authentification associée.
    @OneToOne
    @JoinColumn(name = "id_utilisateur_uuid", referencedColumnName = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    // Instance de BCryptPasswordEncoder pour encoder et vérifier le mot de passe
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
}