package fr.bloc_jo2024;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {

    // Changement du nom de la variable logger pour éviter le conflit
    private static final Logger appLogger = LoggerFactory.getLogger(Main.class);

    // Méthode main pour démarrer l'application Spring Boot
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Méthode de configuration pour le déploiement en servlet container (ex. Tomcat)
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Main.class);
    }

    // Cette méthode est exécutée après la création du bean pour convertir l'URL de la base de données d'Heroku
    @PostConstruct
    public void convertHerokuDatabaseUrl() {
        // Récupérer la variable d'environnement DATABASE_URL
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            try {
                // Convertir l'URL dans un format compatible JDBC pour PostgreSQL
                URI dbUri = new URI(databaseUrl);
                String username = dbUri.getUserInfo().split(":")[0]; // Récupérer le nom d'utilisateur
                String password = dbUri.getUserInfo().split(":")[1]; // Récupérer le mot de passe
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                // Définir les propriétés système pour la connexion à la base de données
                System.setProperty("JDBC_DATABASE_URL", dbUrl);
                System.setProperty("JDBC_DATABASE_USERNAME", username);
                System.setProperty("JDBC_DATABASE_PASSWORD", password);

            } catch (URISyntaxException e) {
                // Utilisation du logger pour enregistrer les erreurs au lieu de System.err
                appLogger.error("Invalid DATABASE_URL format: {}", databaseUrl, e);
            }
        }
    }
}
