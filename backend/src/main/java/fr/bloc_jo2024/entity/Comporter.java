import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ComporterId.class)
public class Comporter {
    @Id
    @ManyToOne
    @JoinColumn(name = "idEpreuve", nullable = false)
    private Epreuve epreuve;

    @Id
    @ManyToOne
    @JoinColumn(name = "idEvenement", nullable = false, referencedColumnName = "idEvenement", onDelete = ReferentialAction.CASCADE)
    private Evenement evenement;

    private boolean jrDeMedaille;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ComporterId implements java.io.Serializable {
    private Long epreuve;
    private Long evenement;
}
