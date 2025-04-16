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
    private Long idTransaction;

    @Column(nullable = false)
    @Min(value = 0, message = "Le montant doit être positif.")
    private double montant;

    @Column(nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransaction statut;

    @OneToOne
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_payement_transaction"))
    private Payement payement;

    // Constructeur pour initialiser la date de transaction
    public Transaction(double montant, StatutTransaction statut, Payement payement) {
        this.montant = montant;
        this.dateTransaction = LocalDateTime.now();
        this.statut = statut;
        this.payement = payement;
    }
}

