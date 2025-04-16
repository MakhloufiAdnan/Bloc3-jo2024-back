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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idUtilisateur;

    @Column(nullable = false, unique = true, length = 250)
    @Email(message = "L'email doit être valide")
    private String email;

    @Column(nullable = false, length = 50)
    @NotNull(message = "Le nom ne peut pas être vide")
    private String nom;

    @Column(nullable = false, length = 50)
    @NotNull(message = "Le prénom ne peut pas être vide")
    private String prenom;

    @Column(nullable = false)
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id-role", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id-adresse", nullable = false)
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