package fr.studi.bloc3jo2024.dto.paiement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitierPaiementDto {
    @NotNull(message = "L'ID du panier est obligatoire")
    private Long idPanier;

    // ID de la méthode de paiement
    @NotNull(message = "L'ID de la méthode de paiement est obligatoire")
    private Long idMethodePayement;

    // Token de paiement
    private String paiementToken;
}