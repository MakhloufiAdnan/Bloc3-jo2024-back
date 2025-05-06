package fr.studi.bloc3jo2024.dto.disciplines;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MettreAJourDisciplineDto {
    @NotNull(message = "L'ID de la discipline est obligatoire")
    private Long idDiscipline;

    @NotBlank(message = "Le nom de la discipline est obligatoire")
    private String nomDiscipline;

    @NotNull(message = "La date de la discipline est obligatoire")
    @Future(message = "La date de la discipline doit être dans le futur")
    private LocalDateTime dateDiscipline;

    @Min(value = 0, message = "Le nombre de places disponibles ne peut pas être négatif")
    private int nbPlaceDispo;

    @NotNull(message = "L'ID de l'adresse est obligatoire")
    private Long idAdresse;
}