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
public class OffreDto {
    private Long id;
    private Long idDiscipline;
    private TypeOffre typeOffre;
    private BigDecimal prix;
    private int capacite;
    private StatutOffre statutOffre;
    private LocalDateTime dateExpiration;
    private int quantiteDisponible;
    private boolean featured;
}