package fr.studi.bloc3jo2024.dto.epreuves;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MettreAJourEpreuveVedetteDto {
    @NotNull(message = "L'ID de l'épreuve est obligatoire")
    private Long idEpreuve;

    @NotNull(message = "Le statut 'en vedette' est obligatoire")
    private Boolean isFeatured;
}