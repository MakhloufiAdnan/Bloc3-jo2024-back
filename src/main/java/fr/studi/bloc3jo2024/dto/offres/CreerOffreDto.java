package fr.studi.bloc3jo2024.dto.offres;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreerOffreDto {

    @NotNull(message = "Le type de l'offre est obligatoire")
    private TypeOffre typeOffre;

    @NotNull(message = "La quantité ne peut pas être nulle.")
    @Min(value = 0, message = "La quantité ne peut pas être négative.") // Permettre 0 si cela signifie "épuisé dès la création" ou si la logique @PrePersist le gère
    private Integer quantite; // Utiliser Integer pour permettre la nullité si @NotNull n'est pas désiré initialement

    @NotNull(message = "Le prix ne peut pas être nul.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix ne peut pas être négatif.")
    private BigDecimal prix;

    /**
     * Date d'expiration spécifique pour l'offre (promotionnelle par exemple).
     * Si non fournie (null), l'expiration sera par défaut celle de la date de la discipline.
     * Doit être dans le futur ou aujourd'hui si spécifiée.
     */
    @FutureOrPresent(message = "La date d'expiration doit être aujourd'hui ou dans le futur.")
    private LocalDateTime dateExpiration; // Optionnel

    @NotNull(message = "Le statut de l'offre est obligatoire lors de la création.")
    private StatutOffre statutOffre;

    @NotNull(message = "L'ID de la discipline est obligatoire.")
    private Long idDiscipline;

    @NotNull(message = "La capacité de l'offre est obligatoire.")
    @Min(value = 1, message = "La capacité doit être au moins 1.")
    private Integer capacite; // Utiliser Integer

    @Builder.Default // Valeur par défaut pour le builder si non spécifié
    private boolean featured = false;
}