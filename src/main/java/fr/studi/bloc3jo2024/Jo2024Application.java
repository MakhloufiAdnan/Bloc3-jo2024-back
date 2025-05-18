package fr.studi.bloc3jo2024; // Assurez-vous que cela correspond à votre package

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class Jo2024Application {

    // Utilisez org.slf4j.Logger, qui est la façade standard utilisée par Spring Boot
    private static final Logger log = LoggerFactory.getLogger(Jo2024Application.class);

    private final Environment env;

    // Injection de Environment par constructeur
    public Jo2024Application(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
		SpringApplication.run(Jo2024Application.class, args);
	}

    @PostConstruct // Cette méthode sera exécutée après que l'injection de dépendances soit terminée
    public void logApplicationProperties() {
        String mailHealthEnabled = env.getProperty("management.health.mail.enabled");
        log.info("--- Profils Actifs (depuis Jo2024Application - PostConstruct) : {} ---", Arrays.toString(env.getActiveProfiles()));
        log.info("--- Valeur de management.health.mail.enabled (depuis Jo2024Application - PostConstruct) : {} ---", mailHealthEnabled);

        // Vous pouvez aussi vérifier les propriétés mail spécifiques que Testcontainers essaie de passer
        log.info("--- SPRING_MAIL_HOST (via env.getProperty) : {} ---", env.getProperty("SPRING_MAIL_HOST"));
        log.info("--- SPRING_MAIL_PORT (via env.getProperty) : {} ---", env.getProperty("SPRING_MAIL_PORT"));
        log.info("--- MANAGEMENT_HEALTH_MAIL_ENABLED (via env.getProperty, pour confirmer) : {} ---", env.getProperty("MANAGEMENT_HEALTH_MAIL_ENABLED"));

    }
}