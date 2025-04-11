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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idTransaction;

    @Column(nullable = false)
    @Min(value = 0, message = "Le montant doit être positif.")
    private double montant;

    @Column(nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransaction statut;

    @ManyToOne
    @JoinColumn(name = "idPayement", nullable = false, foreignKey = @ForeignKey(name = "fk_payement"))
    private Payement payement;

    // Constructeur personnalisé pour initialiser la date de transaction
    public Transaction(double montant, StatutTransaction statut, Payement payement) {
        this.montant = montant;
        this.dateTransaction = LocalDateTime.now();
        this.statut = statut;
        this.payement = payement;
    }
}

enum StatutTransaction {
    REUSSI,
    ECHEC,
    EN_ATTENTE
}