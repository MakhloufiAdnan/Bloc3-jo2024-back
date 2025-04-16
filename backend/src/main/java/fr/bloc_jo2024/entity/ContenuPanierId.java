package fr.bloc_jo2024.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContenuPanierId implements Serializable {
    private Long panier;
    private Long offre;
}