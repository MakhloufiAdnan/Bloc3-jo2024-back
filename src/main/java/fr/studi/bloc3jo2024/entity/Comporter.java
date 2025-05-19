package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comporter", indexes = {
        @Index(name = "idx_comporter_jr_de_medaille", columnList = "jr_de_medaille")
})
public class Comporter {

    @EmbeddedId
    private ComporterKey id;

    @Column(name = "jr_de_medaille")
    private Boolean jrDeMedaille;

    /**
     * Relation vers l'entité Epreuve. Fait partie de la clé composite.
     * MapsId indique que l'attribut idEpreuve de EmbeddedId est mappé par cette relation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEpreuve") // Lie à l'attribut idEpreuve de ComporterKey
    @JoinColumn(name = "id_epreuve", nullable = false)
    @ToString.Exclude
    private Epreuve epreuve;

    /**
     * Relation vers l'entité Discipline. Fait partie de la clé composite.
     * MapsId indique que l'attribut idDiscipline de EmbeddedId est mappé par cette relation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDiscipline")
    @JoinColumn(name = "id_discipline", nullable = false)
    @ToString.Exclude
    private Discipline discipline;


    // equals et hashCode basés sur la clé composite (id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comporter comporter = (Comporter) o;
        return Objects.equals(id, comporter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comporter{" +
                "id=" + id +
                ", jrDeMedaille=" + jrDeMedaille +
                '}';
    }
}