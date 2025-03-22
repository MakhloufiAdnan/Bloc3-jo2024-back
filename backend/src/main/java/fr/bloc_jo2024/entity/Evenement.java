import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "evenements",
        indexes = {@Index(name = "idx_evenements_date", columnList = "date_evenement")}
)
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvenement;

    @Column(nullable = false)
    private LocalDateTime dateEvenement;

    @Column(nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false, onDelete = ForeignKeyAction.CASCADE)
    private Adresse adresse;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL)
    private Set<Offre> offres;
}
