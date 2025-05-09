package fr.studi.bloc3jo2024.dto.paiement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementSimulationResultDto {
    private PaiementDto paiement;
    private Long billetId;
    private String cleFinaleBillet;
}