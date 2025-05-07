package fr.studi.bloc3jo2024.dto.paiement;

import fr.studi.bloc3jo2024.dto.TransactionDto;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import fr.studi.bloc3jo2024.entity.enums.StatutPaiement;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDto {
    private Long idPaiement;
    private Long idPanier;
    private BigDecimal montant;
    private StatutPaiement statutPaiement;
    private MethodePaiementEnum methodePaiement;
    private LocalDateTime datePaiement;
    private TransactionDto transaction;
    @NotNull
    private UUID utilisateurId;
}