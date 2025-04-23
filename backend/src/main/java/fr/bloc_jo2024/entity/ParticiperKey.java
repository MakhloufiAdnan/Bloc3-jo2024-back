package fr.bloc_jo2024.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticiperKey implements Serializable {

    // Partie de la clé primaire composite référençant l'ID du pays.
    private Long idPays;

    // Partie de la clé primaire composite référençant l'ID de l'événement.
    private Long idEvenement;
}