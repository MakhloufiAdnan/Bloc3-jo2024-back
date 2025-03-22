import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Telephone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPhone;

    @Enumerated(EnumType.STRING)
    private TelEnum typeTel;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroTelephone;

    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false, onDelete = ForeignKeyAction.CASCADE)
    private Utilisateur utilisateur;
}

enum TelEnum {
    MOBILE, FIXE;
}