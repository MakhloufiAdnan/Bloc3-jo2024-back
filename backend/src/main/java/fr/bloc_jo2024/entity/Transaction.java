import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaction;

    @Column(nullable = false)
    private double montant;

    @Column(nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransaction statut;

    @ManyToOne
    @JoinColumn(name = "idPayement", nullable = false, onDelete = ForeignKey.CASCADE)
    private Payement payement;
}

enum StatutTransaction {
    REUSSI,
    ECHEC,
    EN_ATTENTE
}