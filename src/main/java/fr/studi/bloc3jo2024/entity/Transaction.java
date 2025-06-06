package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
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
    private BigDecimal montant;

    @Column(name = "date_transaction", nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    // statut de la transaction (REUSSI, ECHEC, EN_ATTENTE)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_transaction", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StatutTransaction statutTransaction;

    // Ajout : Date de validation du paiement si REUSSI
    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    // Ajout : Contenu JSON simulé de retour prestataire (Stripe par exemple)
    @Lob
    @Column(name = "details_transaction", columnDefinition = "TEXT")
    private String details;

    // Ajout : Indique s'il s'agit d'un test
    @Column(name = "is_test", nullable = false)
    private boolean isTest = false;

    // Relation One-to-One vers l'entité Payement. Chaque transaction est liée à un paiement unique.
    @OneToOne
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_payement_transaction"))
    private Paiement paiement;
}