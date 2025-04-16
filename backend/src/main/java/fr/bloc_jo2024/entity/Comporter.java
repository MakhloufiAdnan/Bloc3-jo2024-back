package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comporter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comporter {

    // Clé composite composée de l'ID de l'épreuve et de l'ID de l'événement
    @EmbeddedId
    private ComporterKey id;

    // Attribut pour indiquer si c'est la journée de médaille.
    @Column(name = "jr_de_medaille")
    private Boolean jrDeMedaille;

    // Relation vers l'entité Epreuve. Lier l'attribut idEpreuve de la clé composite à cette relation.
    @ManyToOne
    @MapsId("idEpreuve")
    @JoinColumn(name = "id_epreuve", nullable = false)
    private Epreuve epreuve;

    // Relation vers l'entité Evenement. Lier l'attribut idEvenement de la clé composite à cette relation.
    @ManyToOne
    @MapsId("idEvenement")
    @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;
}