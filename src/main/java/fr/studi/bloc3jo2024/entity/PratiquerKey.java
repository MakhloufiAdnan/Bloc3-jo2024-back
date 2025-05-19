package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité Pratiquer.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PratiquerKey implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "id_athlete")
    private Long idAthlete;

    @Column(name = "id_epreuve")
    private Long idEpreuve;
}