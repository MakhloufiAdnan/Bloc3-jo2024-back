package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
    private LocalDateTime datePayement = LocalDateTime.now(); // Initialisation automatique

    @Column(nullable = false)
    private boolean paiementReussi; // Nom plus clair

    @Column(nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false)
    @Min(value = 0, message = "Le montant payé doit être positif.") // Validation sur montant
    private double montantPaye;

    @ManyToOne
    @JoinColumn(name = "idMethode", nullable = false)
    private MethodePayement methodePayement;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idPanier", nullable = false, referencedColumnName = "idPanier", foreignKey = @ForeignKey(name = "fk_panier"))
    private Panier panier;
}
