package fr.studi.bloc3jo2024.dto.offres;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffreAdminDto {
    private Long id;
    private TypeOffre typeOffre;
    private int quantite;
    private BigDecimal prix;
    private LocalDateTime dateExpiration;
    private StatutOffre statutOffre;
    private int capacite;
    private Long idDiscipline;
    private int nombreDeVentes;
    private boolean featured;
}