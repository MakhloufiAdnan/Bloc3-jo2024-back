package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Participer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participer {
    @EmbeddedId
    private ParticiperKey id;

    @ManyToOne
    @MapsId("idPays")
    @JoinColumn(name = "idPays")
    private Pays pays;

    @ManyToOne
    @MapsId("idEvenement")
    @JoinColumn(name = "idEvenement")
    private Evenement evenement;
}