package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PratiquerKey implements Serializable {
    @Column(name = "id_athlete")
    private Long idAthlete;

    @Column(name = "id_epreuve")
    private Long idEpreuve;
}