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
public class MettreAJourOffreDto {

    // Pour une mise à jour, les champs sont souvent optionnels.
    // Si un champ est fourni, il est mis à jour. S'il est null, il n'est pas touché (logique à implémenter dans le service).
    // Cependant, si certaines valeurs ne peuvent pas être changées en null ou doivent respecter des contraintes
    // même lors d'une mise à jour, les annotations de validation restent pertinentes.
    // Les annotations @NotNull ici impliquent que ces champs DOIVENT être fournis dans la requête de mise à jour.

    @NotNull(message = "Le type de l'offre est obligatoire pour la mise à jour.")
    private TypeOffre typeOffre;

    @NotNull(message = "La quantité ne peut pas être nulle.")
    @Min(value = 0, message = "La quantité ne peut pas être négative.")
    private Integer quantite;

    @NotNull(message = "Le prix ne peut pas être nul.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix ne peut pas être négatif.")
    private BigDecimal prix;

    /**
     * Date d'expiration spécifique pour l'offre.
     * Si fournie, doit être dans le futur ou aujourd'hui.
     * Si non fournie (null), la date d'expiration existante sur l'offre pourrait être conservée
     * ou la logique pourrait se rabattre sur la date de la discipline.
     * Le service devra gérer comment traiter un null ici (garder l'ancienne valeur, ou la supprimer).
     */
    @FutureOrPresent(message = "La date d'expiration doit être aujourd'hui ou dans le futur.")
    private LocalDateTime dateExpiration;

    @NotNull(message = "Le statut de l'offre est obligatoire pour la mise à jour.")
    private StatutOffre statutOffre;

    @NotNull(message = "L'ID de la discipline est obligatoire pour la mise à jour.")
    private Long idDiscipline;

    @NotNull(message = "La capacité de l'offre est obligatoire pour la mise à jour.")
    @Min(value = 1, message = "La capacité doit être au moins 1.")
    private Integer capacite;

    private boolean featured;
}