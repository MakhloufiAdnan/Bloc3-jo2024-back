package fr.studi.bloc3jo2024.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

    /**
     * Instance unique et partagée du conteneur PostgreSQL.
     * {@code static final} assure qu'il n'y a qu'une seule instance pour toute la JVM des tests.
     * Le démarrage se fait dans le bloc static.
     * {@code withReuse(true)} tente d'activer la réutilisation du conteneur entre les exécutions
     * de la suite de tests, si l'environnement le supporte (nécessite une configuration Testcontainers globale).
     */
    protected static final PostgreSQLContainer<?> POSTGRES_DB_CONTAINER;

    static {
        // Initialisation et démarrage du conteneur PostgreSQL.
        // Ceci est exécuté une seule fois lorsque la classe est chargée par la JVM.
        POSTGRES_DB_CONTAINER = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("test_db_shared_" + UUID.randomUUID().toString().substring(0, 8)) // Nom de DB unique
                .withUsername("testuser_shared")
                .withPassword("testpass_shared")
                .withReuse(true); // Active la tentative de réutilisation du conteneur.

        // Démarrage explicite du conteneur.
        POSTGRES_DB_CONTAINER.start();
    }

    /**
     * Configure dynamiquement les propriétés de la source de données Spring
     * pour qu'elles pointent vers le conteneur Testcontainers qui vient d'être démarré.
     * Cette méthode est appelée par Spring avant que le contexte d'application du test ne soit créé.
     *
     * @param registry Le registre où ajouter les propriétés dynamiques.
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_DB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_DB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_DB_CONTAINER::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        // Propriétés optionnelles pour l'initialisation avec schema.sql/data.sql
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always");
    }
}