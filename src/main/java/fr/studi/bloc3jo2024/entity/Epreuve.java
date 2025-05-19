package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "epreuves", indexes = {
        @Index(name = "idx_epreuve_nom_epreuve", columnList = "nom_epreuve")
})
public class Epreuve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_epreuve")
    private Long idEpreuve;

    @Column(name = "nom_epreuve")
    private String nomEpreuve;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFeatured = false;

    /**
     * Relation vers l'entité d'association Comporter, liant cette épreuve à des disciplines.
     * Le FetchType est LAZY par défaut pour @OneToMany.
     * CascadeType.ALL et orphanRemoval = true signifient que les entités Comporter dépendantes
     * seront gérées avec le cycle de vie de Epreuve.
     */
    @OneToMany(mappedBy = "epreuve", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<Comporter> comporte = new HashSet<>();

    // equals et hashCode basés sur l'ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epreuve epreuve = (Epreuve) o;
        if (idEpreuve == null && epreuve.idEpreuve == null) return super.equals(o);
        return Objects.equals(idEpreuve, epreuve.idEpreuve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEpreuve);
    }

    @Override
    public String toString() {
        return "Epreuve{" +
                "idEpreuve=" + idEpreuve +
                ", nomEpreuve='" + nomEpreuve + '\'' +
                ", isFeatured=" + isFeatured +
                '}';
    }
}