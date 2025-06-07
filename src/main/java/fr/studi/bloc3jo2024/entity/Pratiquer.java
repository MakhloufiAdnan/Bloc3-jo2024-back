package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "pratiquer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pratiquer {
    @EmbeddedId
    private PratiquerKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAthlete")
    @JoinColumn(name = "id_athlete", nullable = false)
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEpreuve")
    @JoinColumn(name = "id_epreuve", nullable = false)
    private Epreuve epreuve;

    @Override
    public String toString() {
        return "Pratiquer{" +
                "id=" + (id != null ? "AthleteID:" + id.getIdAthlete() + ",EpreuveID:" + id.getIdEpreuve() : "null") +
                (athlete != null ? ", athleteId=" + athlete.getIdAthlete() : "") +
                (epreuve != null ? ", epreuveId=" + epreuve.getIdEpreuve() : "") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pratiquer that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
