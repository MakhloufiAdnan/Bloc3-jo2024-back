package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "utilisateurs", indexes = @Index(name = "idx_utilisateurs_email", columnList = "email"))
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_utilisateur_UUID")
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

    // Active l’utilisateur après validation email
    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @ManyToOne
    @JoinColumn(name = "id_role_join", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_adresse_join", nullable = false)
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