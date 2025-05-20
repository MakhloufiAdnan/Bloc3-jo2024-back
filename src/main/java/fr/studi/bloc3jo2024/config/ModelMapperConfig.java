package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.entity.ContenuPanier;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Discipline.class, DisciplineDto.class).addMappings(mapper -> mapper.map(
                src -> (src.getAdresse() != null) ? src.getAdresse().getIdAdresse() : null,
                DisciplineDto::setIdAdresse)
        );

        modelMapper.createTypeMap(ContenuPanier.class, ContenuPanierDto.class)
                .addMappings(mapper -> {

                    mapper.map(ModelMapperConfig::helperGetOffreId, ContenuPanierDto::setIdOffre);
                    mapper.map(ModelMapperConfig::helperGetTypeOffre, ContenuPanierDto::setTypeOffre);
                    mapper.map(ModelMapperConfig::helperGetPrixUnitaire, ContenuPanierDto::setPrixUnitaire);
                    mapper.map(ModelMapperConfig::helperGetPrixTotalOffre, ContenuPanierDto::setPrixTotalOffre);
                });

        return modelMapper;
    }

    /**
     * Helper pour extraire l'ID de l'Offre d'un ContenuPanier, gère les cas null.
     * Supposant que ContenuPanier.getOffre() retourne une entité Offre avec getIdOffre().
     */
    private static Long helperGetOffreId(ContenuPanier source) {
        Offre offre = source.getOffre();
        if (offre != null) {
            return offre.getIdOffre();
        }
        return null;
    }

    /**
     * Helper pour extraire le type de l'Offre d'un ContenuPanier, gère les cas null.
     */
    private static TypeOffre helperGetTypeOffre(ContenuPanier source) {
        Offre offre = source.getOffre();
        if (offre != null) {
            return offre.getTypeOffre();
        }
        return null;
    }

    /**
     * Helper pour extraire le prix unitaire de l'Offre d'un ContenuPanier, gère les cas null.
     */
    private static BigDecimal helperGetPrixUnitaire(ContenuPanier source) {
        Offre offre = source.getOffre();
        if (offre != null) {
            return offre.getPrix();
        }
        return null;
    }

    /**
     * Helper pour calculer le prix total de l'Offre dans un ContenuPanier.
     * Gère les cas où l'offre ou son prix sont nulls, ou si la quantité est non valide.
     */
    private static BigDecimal helperGetPrixTotalOffre(ContenuPanier source) {
        if (source == null) {
            return BigDecimal.ZERO;
        }
        Offre offre = source.getOffre();
        if (offre == null || offre.getPrix() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal prixUnitaire = offre.getPrix();
        int quantite = source.getQuantiteCommandee();
        if (quantite <= 0) {
            return BigDecimal.ZERO;
        }
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}