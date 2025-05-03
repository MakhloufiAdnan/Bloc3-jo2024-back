package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pratiquer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pratiquer {
    @EmbeddedId
    private PratiquerKey id;

    @ManyToOne
    @MapsId("idAthlete")
    @JoinColumn(name = "id_athlete", nullable = false)
    private Athlete athlete;

    @ManyToOne
    @MapsId("idEpreuve")
    @JoinColumn(name = "id_epreuve", nullable = false)
    private Epreuve epreuve;
}
