package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth")
public class Oauth {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_oauth")
    private UUID idOAuth;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "facebook_id", unique = true)
    private String facebookId;

    @OneToOne
    @JoinColumn(name = "id_utilisateur_join", referencedColumnName = "id_utilisateur_UUID", nullable = false)
    private Utilisateur utilisateur;
}