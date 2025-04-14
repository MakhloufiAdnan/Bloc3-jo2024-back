package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idToken", columnDefinition = "UUID")
    private UUID idToken;

    // Token complémentaire (pour vérification)
    @Column(nullable = false, length = 255, unique = true)
    private String token;

    // Date d'expiration du token
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Mot de passe encodé (donné par PasswordEncoder dans le service)
    @Column(name = "mot_passe", nullable = false, length = 255)
    private String motPasseHache;

    // Sel du mot de passe (pour le hachage)
    @Column(nullable = false, length = 255)
    private String salt;

    // Relation bidirectionnelle pour retrouver l'utilisateur
    @OneToOne(mappedBy = "authentification")
    @JoinColumn(name = "IdUtilisateur_UUID", nullable = false)
    private Utilisateur utilisateur;
}