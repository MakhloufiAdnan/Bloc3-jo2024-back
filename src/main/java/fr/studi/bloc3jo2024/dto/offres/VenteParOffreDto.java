package fr.studi.bloc3jo2024.dto.offres;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteParOffreDto {
    private String nomOffre;
    private long nombreVentes;
}

