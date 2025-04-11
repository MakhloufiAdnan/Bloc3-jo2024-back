package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.Utilisateur;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Oauth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID idOAuth;

    @Column(unique = true, length = 100)
    private String googleId;

    @Column(unique = true, length = 100)
    private String facebookId;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_oauth"))
    private Utilisateur utilisateur;
}