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
@Table(name = "comporter", indexes = {
        @Index(name = "idx_comporter_jr_de_medaille", columnList = "jr_de_medaille")
})
public class Comporter {

    @EmbeddedId
    private ComporterKey id;

    @Column(name = "jr_de_medaille")
    private Boolean jrDeMedaille;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEpreuve")
    @JoinColumn(name = "id_epreuve", nullable = false)
    private Epreuve epreuve;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDiscipline")
    @JoinColumn(name = "id_discipline", nullable = false)
    private Discipline discipline;

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
                (epreuve != null ? ", epreuveId=" + epreuve.getIdEpreuve() : "") +
                (discipline != null ? ", disciplineId=" + discipline.getIdDiscipline() : "") +
                '}';
    }
}