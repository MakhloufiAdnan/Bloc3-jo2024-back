package fr.studi.bloc3jo2024.dto.panier;

import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanierDto {
    private Long idPanier;
    private BigDecimal montantTotal;
    private StatutPanier statut;
    private LocalDateTime dateAjout;

    @NotNull
    private UUID idUtilisateur;

    @Valid
    private List<ContenuPanierDto> contenuPaniers;
}