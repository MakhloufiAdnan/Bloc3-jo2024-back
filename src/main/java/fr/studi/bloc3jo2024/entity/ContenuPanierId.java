package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité contenue du panier.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenuPanierId implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private Long panier;

    private Long offre;
}