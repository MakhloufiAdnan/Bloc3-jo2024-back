package fr.studi.bloc3jo2024.dto.offres;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MettreAJourOffreDto {
    @NotNull(message = "Le type de l'offre est obligatoire")
    private TypeOffre typeOffre;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    private BigDecimal prix;

    private LocalDateTime dateExpiration;

    @NotNull(message = "Le statut de l'offre est obligatoire")
    private StatutOffre statutOffre;

    @NotNull(message = "L'ID de la discipline est obligatoire")
    private Long idDiscipline;

    @NotNull(message = "La capacité de l'offre est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private int capacite;

    private boolean featured;
}