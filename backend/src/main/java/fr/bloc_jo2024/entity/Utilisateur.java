package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "utilisateurs",
        indexes = {@Index(name = "idx_utilisateurs_email", columnList = "email")}
)
public class Utilisateur {
    @Id
    private UUID idUtilisateur;

    @Column(nullable = false, unique = true, length = 250)
    private String email;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "idRole", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false)
    private Adresse adresse;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telephone> telephones;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_authentification", referencedColumnName = "idAuthentification")
    private Authentification authentification;
}
