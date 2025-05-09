package fr.studi.bloc3jo2024.dto.paiement;

import fr.studi.bloc3jo2024.entity.enums.StatutTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long idTransaction;
    private BigDecimal montant;
    private LocalDateTime dateTransaction;
    private StatutTransaction statutTransaction;
    private LocalDateTime dateValidation;
    private String details;
    private boolean isTest;
    private Long idPayement;
}