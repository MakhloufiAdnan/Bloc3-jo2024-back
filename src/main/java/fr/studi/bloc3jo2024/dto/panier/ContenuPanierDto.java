package fr.studi.bloc3jo2024.dto.panier;

import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenuPanierDto {
    private Long idOffre;
    private TypeOffre typeOffre;
    private BigDecimal prixUnitaire;
    private int quantiteCommandee;
    private BigDecimal prixTotalOffre;
}