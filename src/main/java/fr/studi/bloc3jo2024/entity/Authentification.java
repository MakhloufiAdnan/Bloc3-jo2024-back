package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "authentifications")
@Getter
@Setter
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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", referencedColumnName = "id_utilisateur_uuid", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Authentification that)) return false;
        return Objects.equals(idToken, that.idToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idToken);
    }

    @Override
    public String toString() {
        return "Authentification{" +
                ", utilisateurId=" + (utilisateur != null && utilisateur.getIdUtilisateur() != null ? utilisateur.getIdUtilisateur() : "null") +
                '}';
    }
}