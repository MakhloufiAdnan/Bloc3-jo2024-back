package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @Column(name = "montant", nullable = false)
    @Min(value = 0, message = "Le montant doit être positif.")
    private BigDecimal montant;

    @Column(name = "date_transaction", nullable = false)
    @Builder.Default
    private LocalDateTime dateTransaction = LocalDateTime.now();

    // statut de la transaction (REUSSI, ECHEC, EN_ATTENTE)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_transaction", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StatutTransaction statutTransaction;

    // Date de validation du paiement si REUSSI
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    // Contenu JSON simulé de retour prestataire (Stripe par exemple)
    @Lob
    @Column(name = "details_transaction", columnDefinition = "TEXT")
    private String details;

    // Indique s'il s'agit d'un test
    @Column(name = "is_test", nullable = false)
    @Builder.Default
    private boolean isTest = false;

    // Relation One-to-One vers l'entité Payement. Chaque transaction est liée à un paiement unique.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_payement_transaction"))
    private Paiement paiement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        if (idTransaction == null || that.idTransaction == null) {
            return false;
        }
        return Objects.equals(idTransaction, that.idTransaction);
    }

    @Override
    public int hashCode() {
        return idTransaction != null ? Objects.hash(idTransaction) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "idTransaction=" + idTransaction +
                ", montant=" + montant +
                ", dateTransaction=" + dateTransaction +
                ", statutTransaction=" + statutTransaction +
                ", dateValidation=" + dateValidation +
                ", details='" + (details != null ? details.substring(0, Math.min(details.length(), 50)) + "..." : "null") + '\'' +
                ", isTest=" + isTest +
                ", paiementId=" + (paiement != null && paiement.getIdPaiement() != null ? paiement.getIdPaiement() : "null") +
                '}';
    }
}