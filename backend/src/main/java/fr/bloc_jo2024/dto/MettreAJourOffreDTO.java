package fr.bloc_jo2024.dto;

import fr.bloc_jo2024.entity.enums.StatutOffre;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MettreAJourOffreDTO {
    @NotNull(message = "Le type de l'offre est obligatoire")
    private TypeOffre typeOffre;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    private double prix;

    private LocalDateTime dateExpiration;

    @NotNull(message = "Le statut de l'offre est obligatoire")
    private StatutOffre statutOffre;

    @NotNull(message = "L'ID du panier est obligatoire")
    private Long panierId;

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long evenementId;
}