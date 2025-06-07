package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Getter
@Setter
@Builder
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

    @Column(name = "montant", nullable = false, precision = 38, scale = 2)
    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_methode_paiement", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_methode"))
    private MethodePaiement methodePaiement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_utilisateur"))
    private Utilisateur utilisateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_paiement_panier"))
    private Panier panier;

    // Transaction est une entité avec une relation OneToOne vers Paiement
    @OneToOne(mappedBy = "paiement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Transaction transaction;

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", statutPaiement=" + statutPaiement +
                ", datePaiement=" + datePaiement +
                ", montant=" + montant +
                ", methodePaiementId=" + (methodePaiement != null ? methodePaiement.getIdMethode() : null) +
                ", utilisateurId=" + (utilisateur != null ? utilisateur.getIdUtilisateur() : null) +
                ", panierId=" + (panier != null ? panier.getIdPanier() : null) +
                '}';
    }

    @Override
    public int hashCode() {
        return idPaiement != null ? idPaiement.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Paiement paiement = (Paiement) obj;
        return idPaiement != null && idPaiement.equals(paiement.idPaiement);
    }
}