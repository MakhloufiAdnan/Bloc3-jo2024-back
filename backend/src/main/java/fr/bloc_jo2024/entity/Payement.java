import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPayement;

    @Column(nullable = false)
    private LocalDateTime datePayement;
    private boolean statutPayement;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private double montantPaye;

    @ManyToOne
    @JoinColumn(name = "idMethode", nullable = false)
    private MethodePayement methodePayement;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idPanier", nullable = false, referencedColumnName = "idPanier", onDelete = ReferentialAction.CASCADE)
    private Panier panier;
}