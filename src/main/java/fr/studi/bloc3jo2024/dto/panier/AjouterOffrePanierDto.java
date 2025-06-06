package fr.studi.bloc3jo2024.dto.panier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AjouterOffrePanierDto {
    @NotNull(message = "L'ID de l'offre est obligatoire")
    private Long idOffre;

    @Min(value = 1, message = "La quantité commandée doit être au moins 1")
    private int quantite;
}