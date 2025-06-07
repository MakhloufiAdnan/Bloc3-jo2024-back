package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ContenuPanierId.class)
@Table(name = "contenu_panier")
public class ContenuPanier {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_panier"))
    private Panier panier;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_offre"))
    private Offre offre;

    @Column(name = "quantite_commandee", nullable = false)
    private int quantiteCommandee;

    @Override
    public String toString() {
        return "ContenuPanier{" +
                "panierId=" + (panier != null ? panier.getIdPanier() : "null") +
                ", offreId=" + (offre != null ? offre.getIdOffre() : "null") +
                ", quantiteCommandee=" + quantiteCommandee +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContenuPanier that = (ContenuPanier) o;
        return panier != null && panier.getIdPanier() != null &&
                offre != null && offre.getIdOffre() != null &&
                panier.getIdPanier().equals(that.panier != null ? that.panier.getIdPanier() : null) &&
                offre.getIdOffre().equals(that.offre != null ? that.offre.getIdOffre() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                (panier != null ? panier.getIdPanier() : null),
                (offre != null ? offre.getIdOffre() : null)
        );
    }
}