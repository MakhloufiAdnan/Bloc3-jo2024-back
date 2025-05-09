package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.dto.disciplines.DisciplineDto;
import fr.studi.bloc3jo2024.entity.Discipline;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {


    /**
     * Bean pour ModelMapper avec configurations spécifiques.
     *
     * @return une instance de ModelMapper configurée
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Ajout d'un mappage spécifique pour Discipline vers DisciplineDto
        modelMapper.addMappings(new PropertyMap<Discipline, DisciplineDto>() {
            @Override
            protected void configure() {
                // Mappez l'ID de l'objet Adresse imbriqué au champ idAdresse dans le DTO
                map().setIdAdresse(source.getAdresse().getIdAdresse());
            }
        });

        return modelMapper;
    }
}