import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommande;

    @Column(nullable = false, unique = true)
    private String numCommande;

    @Column(nullable = false)
    private boolean envoyeMail;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "idPayement", nullable = false, unique = true)
    private Payement payement;
}