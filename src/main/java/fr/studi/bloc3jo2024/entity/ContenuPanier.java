package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(ContenuPanierId.class)
@Table(name = "contenu_panier")
public class ContenuPanier {

    @Column(name = "quantite_commandee", nullable = false)
    private int quantiteCommandee;

    // Partie de la clé primaire composite
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", referencedColumnName = "id_panier", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_panier"))
    @ToString.Exclude
    private Panier panier;

    // Partie de la clé primaire composite
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre", referencedColumnName = "id_offre", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_offre"))
    @ToString.Exclude
    private Offre offre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContenuPanier that = (ContenuPanier) o;
        return Objects.equals(panier, that.panier) &&
                Objects.equals(offre, that.offre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(panier, offre);
    }

    @Override
    public String toString() {
        return "ContenuPanier{" +
                "quantiteCommandee=" + quantiteCommandee +
                ", panierId=" + (panier != null ? panier.getIdPanier() : "null") +
                ", offreId=" + (offre != null ? offre.getIdOffre() : "null") +
                '}';
    }
}