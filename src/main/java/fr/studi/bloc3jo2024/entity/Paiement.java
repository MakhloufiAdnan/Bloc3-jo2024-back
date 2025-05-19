package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import jakarta.persistence.*;
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
@Table(name = "paiements", indexes = {
        @Index(name = "idx_paiement_utilisateur", columnList = "id_utilisateur"),
        @Index(name = "idx_paiement_panier", columnList = "id_panier", unique = true)
})
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    @NotNull(message = "Le statut du paiement ne peut être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false)
    private StatutPaiement statutPaiement;

    @NotNull(message = "La méthode de paiement ne peut être nulle.")
    @Enumerated(EnumType.STRING)
    @Column(name = "methode_paiement", nullable = false)
    private MethodePaiementEnum methodePaiement;

    @Column(name = "date_paiement", nullable = false)
    @Builder.Default
    private LocalDateTime datePaiement = LocalDateTime.now();

    @NotNull(message = "Le montant du paiement ne peut être nul.")
    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_paiement_utilisateur"))
    @ToString.Exclude
    private Utilisateur utilisateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_paiement_panier"))
    @ToString.Exclude
    private Panier panier;

    @OneToOne(mappedBy = "paiement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Transaction transaction;


    @PrePersist
    protected void onPersist() {
        if (datePaiement == null) {
            datePaiement = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paiement paiement)) return false;
        if (this.idPaiement == null || paiement.idPaiement == null) {
            return false;
        }
        return Objects.equals(idPaiement, paiement.idPaiement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPaiement);
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", statutPaiement=" + statutPaiement +
                ", methodePaiement=" + methodePaiement +
                ", datePaiement=" + datePaiement +
                ", montant=" + montant +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                (panier != null ? ", panierId=" + panier.getIdPanier() : "") +
                '}';
    }
}