package fr.studi.bloc3jo2024.dto.panier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModifierContenuPanierDto {
    @NotNull(message = "L'ID de l'offre dans le panier est obligatoire")
    private Long idOffre;

    @Min(value = 1, message = "La nouvelle quantité doit être au moins 1")
    private int nouvelleQuantite;
}