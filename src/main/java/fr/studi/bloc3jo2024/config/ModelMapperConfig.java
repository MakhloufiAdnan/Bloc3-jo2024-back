package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.dto.panier.ContenuPanierDto;
import fr.studi.bloc3jo2024.entity.ContenuPanier;
import fr.studi.bloc3jo2024.entity.Discipline;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Mapping pour Discipline vers DisciplineDto
        modelMapper.addMappings(new PropertyMap<Discipline, DisciplineDto>() {
            @Override
            protected void configure() {
                map().setIdDiscipline(source.getIdDiscipline());
                map().setNomDiscipline(source.getNomDiscipline());
                // Ajoutez d'autres mappings pour les champs de DisciplineDto si nécessaire
            }
        });

        // Mapping pour ContenuPanier vers ContenuPanierDto
        modelMapper.addMappings(new PropertyMap<ContenuPanier, ContenuPanierDto>() {
            @Override
            protected void configure() {
                // Mapper la quantité directement : ne peut pas être null
                map().setQuantiteCommandee(source.getQuantiteCommandee());

                // Utiliser les méthodes helpers avec un cast explicite dans les lambdas
                using(context -> getOffreId((ContenuPanier) context.getSource())).map().setIdOffre(source.getOffre().getIdOffre());
                using(context -> getPrixUnitaire((ContenuPanier) context.getSource())).map().setPrixUnitaire(source.getOffre().getPrix());
                using(context -> getTypeOffre((ContenuPanier) context.getSource())).map().setTypeOffre(source.getOffre().getTypeOffre());
                using(context -> getPrixTotalOffre((ContenuPanier) context.getSource())).map(source).setPrixTotalOffre(null);

                // Le chemin 'source.getOffre().getIdOffre()' etc. est toujours nécessaire pour ModelMapper
                // La VALEUR mappée sera celle retournée par le convertisseur.
            }
        });

        return modelMapper;
    }

    // --- Méthodes Helpers pour les Convertisseurs ---

    /**
     * Helper pour extraire l'ID de l'Offre d'un ContenuPanier, gère les cas null.
     */
    private static Long getOffreId(ContenuPanier source) {
        // Complexité: 1 (ternaire) ou 2 (si ?:) + 1 (&&) = 2 ou 3
        return (source != null && source.getOffre() != null) ? source.getOffre().getIdOffre() : null;
    }

    /**
     * Helper pour extraire le prix unitaire de l'Offre d'un ContenuPanier, gère les cas null.
     */
    private static BigDecimal getPrixUnitaire(ContenuPanier source) {
        // Complexité: 1 (ternaire) ou 2 (si ?:) + 1 (&&) = 2 ou 3
        return (source != null && source.getOffre() != null) ? source.getOffre().getPrix() : null;
    }

    /**
     * Helper pour extraire le type de l'Offre d'un ContenuPanier, gère les cas null.
     */
    private static TypeOffre getTypeOffre(ContenuPanier source) {
        // Complexité: 1 (ternaire) ou 2 (si ?:) + 1 (&&) = 2 ou 3
        return (source != null && source.getOffre() != null) ? source.getOffre().getTypeOffre() : null;
    }


    /**
     * Helper pour calculer le prix total de l'Offre dans un ContenuPanier, gère les cas null.
     * Refactorisé pour potentiellement réduire la complexité perçue.
     */
    private static BigDecimal getPrixTotalOffre(ContenuPanier source) { // Complexité réduite par rapport à un seul if multi-condition
        // Complexité: 1 + 1 + 1 = 3 (un point pour chaque if)
        if (source == null) { // +1
            return BigDecimal.ZERO;
        }
        Offre offre = source.getOffre();
        if (offre == null) { // +1
            return BigDecimal.ZERO;
        }
        BigDecimal prix = offre.getPrix();
        if (prix == null) { // +1
            return BigDecimal.ZERO;
        }
        // Si on arrive ici, source, offre et prix ne sont pas null
        return prix.multiply(BigDecimal.valueOf(source.getQuantiteCommandee()));
    }

    // --- Fin des Méthodes Helpers ---
}