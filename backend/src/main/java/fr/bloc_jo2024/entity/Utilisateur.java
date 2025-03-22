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
    @Temporal(TemporalType.DATE)
    private LocalDate dateNaissance;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "idRole", nullable = false, onDelete = ForeignKeyAction.CASCADE)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false, onDelete = ForeignKeyAction.CASCADE)
    private Adresse adresse;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Telephone> telephones;
}
