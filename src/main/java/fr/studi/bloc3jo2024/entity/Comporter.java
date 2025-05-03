package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comporter", indexes = {
        @Index(name = "idx_comporter_jr_de_medaille", columnList = "jr_de_medaille")
})
public class Comporter {

    // Clé composite composée de l'ID de l'épreuve et de l'ID de la discipline
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

    // Relation vers l'entité Discipline. Lier l'attribut idDiscipline de la clé composite à cette relation.
    @ManyToOne
    @MapsId("idDiscipline")
    @JoinColumn(name = "id_discipline", nullable = false)
    private Discipline discipline;
}