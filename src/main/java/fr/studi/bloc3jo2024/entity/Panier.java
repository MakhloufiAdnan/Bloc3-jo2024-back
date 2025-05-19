package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paniers", indexes = {
        @Index(name = "idx_paniers_statut", columnList = "statut_panier")
})
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_panier")
    private Long idPanier;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_panier", nullable = false)
    private StatutPanier statut;

    @Column(name = "date_ajout", nullable = false)
    @Builder.Default
    private LocalDateTime dateAjout = LocalDateTime.now();

    /**
     * Utilisateur à qui appartient le panier.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    @ToString.Exclude
    private Utilisateur utilisateur;

    /**
     * Ensemble des éléments contenus dans le panier.
     */
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();

    /**
     * Paiement associé à ce panier (si payé).
     */
    @OneToOne(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Paiement paiement;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (dateAjout == null) {
            dateAjout = LocalDateTime.now();
        }
        if (montantTotal == null) {
            montantTotal = BigDecimal.ZERO;
        }
        if (statut == null) {
            statut = StatutPanier.EN_ATTENTE;
        }
    }

    // equals et hashCode basés sur l'ID métier (idPanier)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Panier panier)) return false;
        if (idPanier == null && panier.idPanier == null) return super.equals(o);
        return Objects.equals(idPanier, panier.idPanier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPanier);
    }

    @Override
    public String toString() {
        return "Panier{" +
                "idPanier=" + idPanier +
                ", montantTotal=" + montantTotal +
                ", statut=" + statut +
                ", dateAjout=" + dateAjout +
                ", version=" + version +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                '}';
    }
}