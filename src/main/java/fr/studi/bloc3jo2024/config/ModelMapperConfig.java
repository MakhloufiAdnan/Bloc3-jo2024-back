package fr.studi.bloc3jo2024.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    /**
     * Bean pour ModelMapper.
     *
     * @return une nouvelle instance de ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}