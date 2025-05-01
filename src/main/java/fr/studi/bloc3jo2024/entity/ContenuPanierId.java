package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContenuPanierId implements Serializable {

    // Partie de la clé primaire composite référençant l'ID du panier.
    private Long panier;

    // Partie de la clé primaire composite référençant l'ID de l'offre.
    private Long offre;
}