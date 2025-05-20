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
 * DTO représentant une offre avec des détails supplémentaires pour l'administration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreAdminDto {
    private Long id;
    private TypeOffre typeOffre;
    private int quantite;
    private BigDecimal prix;

    /**
     * Date d'expiration effective de l'offre, calculée.
     */
    private LocalDateTime dateExpiration;
    private StatutOffre statutOffre;
    private int capacite; // Capacité de l'offre (ex: 2 personnes)
    private Long idDiscipline;
    private boolean featured;

    /**
     * Nombre total de ventes (billets) pour cette offre.
     * Ce champ sera typiquement peuplé par une logique de service spécifique.
     */
    @Builder.Default
    private int nombreDeVentes = 0;
}