package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oauth {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idOAuth;

    @Column(unique = true)
    private String googleId;

    @Column(unique = true)
    private String facebookId;

    @OneToOne
    @JoinColumn(name = "idUtilisateur", nullable = false)
    private Utilisateur utilisateur;
}