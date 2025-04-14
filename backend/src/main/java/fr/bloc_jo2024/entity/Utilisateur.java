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
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Relation vers le rôle (ADMIN ou USER)
    @ManyToOne
    @JoinColumn(name = "idRole", nullable = false)
    private Role role;

    // Relation vers l'adresse
    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false)
    private Adresse adresse;

    // Collection de téléphones
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Telephone> telephones;

    // Association avec l'authentification
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_authentification", referencedColumnName = "idToken")
    private Authentification authentification;
}