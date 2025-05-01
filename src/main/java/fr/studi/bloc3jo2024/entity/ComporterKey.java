package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComporterKey implements Serializable {

    // Partie de la clé primaire composite référençant l'ID de l'épreuve.
    @Column(name = "id_epreuve")
    private Long idEpreuve;

    // Partie de la clé primaire composite référençant l'ID de l'événement.
    @Column(name = "id_evenement")
    private Long idEvenement;
}