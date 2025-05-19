package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "utilisateurs",
        indexes = {
                @Index(name = "idx_utilisateurs_email", columnList = "email", unique = true),
                @Index(name = "idx_utilisateurs_id_adresse", columnList = "id_adresse")
        })
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_utilisateur_uuid")
    private UUID idUtilisateur;

    @NotNull(message = "L'email ne peut pas être vide.")
    @Email(message = "L'email doit être valide.")
    @Column(name = "email", nullable = false, unique = true, length = 250)
    private String email;

    @NotNull(message = "Le nom ne peut pas être vide.")
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @NotNull(message = "Le prénom ne peut pas être vide.")
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @NotNull(message = "La date de naissance ne peut pas être vide.")
    @Past(message = "La date de naissance doit être dans le passé.")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "cle_utilisateur", unique = true)
    @ToString.Exclude
    private String cleUtilisateur;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateurs_roles"))
    @ToString.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_adresse", foreignKey = @ForeignKey(name = "fk_utilisateurs_adresses"))
    @ToString.Exclude
    private Adresse adresse;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Telephone> telephones = new ArrayList<>();

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Authentification authentification;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<AuthTokenTemporaire> authTokensTemporaires = new ArrayList<>();

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Oauth oauth;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Panier> paniers = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @Builder.Default
    private List<Billet> billets = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur that)) return false;
        if (this.idUtilisateur == null || that.idUtilisateur == null){
            return false;
        }
        return Objects.equals(idUtilisateur, that.idUtilisateur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtilisateur);
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", dateCreation=" + dateCreation +
                ", isVerified=" + isVerified +
                (role != null ? ", roleId=" + role.getIdRole() : ", role=null") +
                (adresse != null ? ", adresseId=" + adresse.getIdAdresse() : ", adresse=null") +
                '}';
    }
}