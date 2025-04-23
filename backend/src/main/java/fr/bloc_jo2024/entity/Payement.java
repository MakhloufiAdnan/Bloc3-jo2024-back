package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payement")
    private Long idPayement;

    @Column(name = "date_payement", nullable = false)
    private LocalDateTime datePayement = LocalDateTime.now();

    @Column(name = "paiement_reussi", nullable = false)
    private boolean paiementReussi;

    @Column(name = "transaction", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "montant_paye", nullable = false)
    @Min(value = 0, message = "Le montant payé doit être positif.")
    private double montantPaye;

    // Relation Many-to-One vers l'entité MethodePayement. Un payement a plusieurs méthode de payement
    @ManyToOne
    @JoinColumn(name = "id_methode", nullable = false)
    private MethodePayement methodePayement;

    // Relation One-to-One vers l'entité Panier. Chaque paiement est lié à un panier unique.
    @OneToOne
    @JoinColumn(name = "id_panier", nullable = false, referencedColumnName = "id_panier", foreignKey = @ForeignKey(name = "fk_panier"))
    private Panier panier;

    // Relation One-to-One inversée vers l'entité Transaction. Un payement a une seule transaction associée.
    @OneToOne(mappedBy = "payement")
    private Transaction transaction;
}