package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.StatutTransaction;
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
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @Column(name = "montant", nullable = false)
    @Min(value = 0, message = "Le montant doit être positif.")
    private double montant;

    @Column(name = "date_transaction", nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    // statut de la transaction (REUSSI, ECHEC, EN_ATTENTE)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_transaction", nullable = false)
    private StatutTransaction statutTransaction;

    // Relation One-to-One vers l'entité Payement. Chaque transaction est liée à un paiement unique.
    @OneToOne
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_payement_transaction"))
    private Payement payement;

    // Constructeur pour initialiser la date de transaction
    public Transaction(double montant, StatutTransaction statut, Payement payement) {
        this.montant = montant;
        this.dateTransaction = LocalDateTime.now();
        this.statutTransaction = statut;
        this.payement = payement;
    }
}

