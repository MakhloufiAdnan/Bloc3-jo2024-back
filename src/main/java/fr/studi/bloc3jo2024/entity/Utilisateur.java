package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "utilisateurs",
        indexes = {
                @Index(name = "idx_utilisateurs_email", columnList = "email"),
                @Index(name = "idx_utilisateurs_adresse", columnList = "id_adresse")
        })
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_utilisateur_uuid")
    private UUID idUtilisateur;

    @Column(name = "email", nullable = false, unique = true, length = 250)
    @Email(message = "L'email doit être valide")
    private String email;

    @Column(name = "nom", nullable = false, length = 50)
    @NotNull(message = "Le nom ne peut pas être vide")
    private String nom;

    @Column(name = "prenom", nullable = false, length = 50)
    @NotNull(message = "Le prénom ne peut pas être vide")
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @Column(name = "date_creation",nullable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Clé unique générée après la validation du compte, utilisée pour l'achat des e-billets.
    @Column(name = "cle_utilisateur", unique = true)
    private String cleUtilisateur;

    // Active l’utilisateur après validation email
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateurs_roles"))
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_adresse", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateurs_adresses"))
    private Adresse adresse;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Telephone> telephones;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private Authentification authentification;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthTokenTemporaire> authTokensTemporaires;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private Oauth oauth;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Panier> paniers;

    @OneToMany(mappedBy = "utilisateur")
    private List<Billet> billets;
}