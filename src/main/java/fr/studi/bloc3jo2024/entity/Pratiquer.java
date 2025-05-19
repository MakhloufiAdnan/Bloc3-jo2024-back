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
@Table(name = "pratiquer")
public class Pratiquer {

    @EmbeddedId
    private PratiquerKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAthlete")
    @JoinColumn(name = "id_athlete", nullable = false)
    @ToString.Exclude
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEpreuve")
    @JoinColumn(name = "id_epreuve", nullable = false)
    @ToString.Exclude
    private Epreuve epreuve;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pratiquer pratiquer = (Pratiquer) o;
        return Objects.equals(id, pratiquer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pratiquer{" +
                "id=" + id +
                '}';
    }
}