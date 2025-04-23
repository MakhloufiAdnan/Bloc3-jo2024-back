package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participer {
    @EmbeddedId
    private ParticiperKey id;

    // Relation Many-to-One vers l'entité Pays.
    @ManyToOne
    @MapsId("idPays")
    @JoinColumn(name = "id_pays")
    private Pays pays;

    // Relation Many-to-One vers l'entité Evenement.
    @ManyToOne
    @MapsId("idEvenement")
    @JoinColumn(name = "id_evenement")
    private Evenement evenement;
}