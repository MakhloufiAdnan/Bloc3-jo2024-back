package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComporterKey implements Serializable {
    private Long idEpreuve;
    private Long idEvenement;
}