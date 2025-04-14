package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticiperKey implements Serializable {
    private Integer idPays;
    private Integer idEvenement;
}