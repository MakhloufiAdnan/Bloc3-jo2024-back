package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_paiement", columnList = "id_payement", unique = true)
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @NotNull(message = "Le montant de la transaction ne peut pas être nul.")
    @Min(value = 0, message = "Le montant doit être positif ou nul.")
    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "date_transaction", nullable = false)
    @Builder.Default
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @NotNull(message = "Le statut de la transaction ne peut être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_transaction", nullable = false)
    private StatutTransaction statutTransaction;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Lob
    @Column(name = "details_transaction", columnDefinition = "TEXT")
    @ToString.Exclude
    private String details;

    @Column(name = "is_test", nullable = false)
    @Builder.Default
    private boolean isTest = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_transaction_paiement"))
    @ToString.Exclude
    private Paiement paiement;

    @PrePersist
    protected void onPrePersist() {
        if (dateTransaction == null) {
            dateTransaction = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        if (this.idTransaction == null || that.idTransaction == null) {
            return false;
        }
        return Objects.equals(idTransaction, that.idTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTransaction);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "idTransaction=" + idTransaction +
                ", montant=" + montant +
                ", dateTransaction=" + dateTransaction +
                ", statutTransaction=" + statutTransaction +
                ", dateValidation=" + dateValidation +
                ", isTest=" + isTest +
                (paiement != null ? ", paiementId=" + paiement.getIdPaiement() : ", paiement=null") +
                '}';
    }
}