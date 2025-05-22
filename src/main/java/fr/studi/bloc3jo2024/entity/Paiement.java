package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    // Statut du paiement (EN_ATTENTE, PAYE, ANNULE).
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StatutPaiement statutPaiement;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_methode_paiement", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_methode"))
    private MethodePaiement methodePaiement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_utilisateur"))
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_paiement_panier"))
    @EqualsAndHashCode.Exclude
    private Panier panier;

    // Transaction est une entité avec une relation OneToOne vers Paiement
    @OneToOne(mappedBy = "paiement", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private Transaction transaction;
}