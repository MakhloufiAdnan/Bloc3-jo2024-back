package fr.studi.bloc3jo2024.dto.offres;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO représentant une offre telle qu'affichée à un utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreDto {
    private Long id;
    private Long idDiscipline;
    private TypeOffre typeOffre;
    private BigDecimal prix;
    private int capacite;
    private StatutOffre statutOffre;

    /**
     * Date d'expiration effective de l'offre, calculée en prenant en compte
     * la date d'expiration propre à l'offre et la date de la discipline.
     */
    private LocalDateTime dateExpiration;

    /**
     * Quantité d'unités de cette offre encore disponibles à la vente.
     * Peut différer de la capacité de la discipline.
     */
    private int quantiteDisponible;
    private boolean featured;
}