package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauths")
public class Oauth {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_oauth")
    private UUID idOAuth;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "facebook_id", unique = true)
    private String facebookId;

    // Relation One-to-One vers l'entité Utilisateur. Chaque compte OAuth est lié à un utilisateur.
    @OneToOne
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;
}