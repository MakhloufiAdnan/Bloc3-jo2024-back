package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité Comporter.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComporterKey implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "id_epreuve")
    private Long idEpreuve;

    @Column(name = "id_discipline")
    private Long idDiscipline;
}