package fr.studi.bloc3jo2024.dto.disciplines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineDto {
    private Long idDiscipline;
    private String nomDiscipline;
    private LocalDateTime dateDiscipline;
    private int nbPlaceDispo;
    private Long idAdresse;
}