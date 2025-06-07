package fr.studi.bloc3jo2024.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe de base abstraite pour les tests d'intégration utilisant Testcontainers.
 * <p>
 * Cette classe active le profil "integration" et configure un **unique conteneur PostgreSQL**
 * qui sera partagé par toutes les classes de test qui l'étendent.
 * <p>
 * En utilisant un bloc d'initialisation statique (le "singleton container pattern"),
 * nous nous assurons que le conteneur n'est démarré qu'une seule fois pour toute la durée
 * de l'exécution des tests, ce qui élimine les erreurs de connexion entre les classes de test
 * et améliore considérablement la performance.
 * </p>
 */
@Testcontainers
@ActiveProfiles("integration")
public abstract class AbstractPostgresIntegrationTest {

    // Déclaration du conteneur. Il est 'static' pour être partagé par toutes les instances de test.
    // Il est 'final' car il sera initialisé une seule fois dans le bloc statique.
    static final PostgreSQLContainer<?> postgresDBContainer;

    // Bloc d'initialisation statique.
    // Ce bloc est exécuté UNE SEULE FOIS, lorsque la JVM charge la classe.
    static {
        // 1. Instanciation du conteneur avec sa configuration.
        postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("test_db_integration")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true) // Garder cette option pour réutiliser le conteneur entre les builds Maven/Gradle
                .withInitScript("init.sql"); // Le script de création du schéma est toujours crucial

        // 2. Démarrage manuel du conteneur.
        // Il restera actif jusqu'à ce que la JVM s'arrête.
        postgresDBContainer.start();
    }


    /**
     * Configure dynamiquement les propriétés de la source de données de Spring
     * pour qu'elles pointent vers le conteneur PostgreSQL unique démarré ci-dessus.
     * Cette méthode est exécutée avant la création du contexte de l'application.
     *
     * @param registry Le registre de propriétés dynamique de Spring.
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        // L'URL JDBC, le nom d'utilisateur et le mot de passe sont récupérés du conteneur
        // qui est maintenant garanti d'être en cours d'exécution.
        registry.add("spring.datasource.url", postgresDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDBContainer::getUsername);
        registry.add("spring.datasource.password", postgresDBContainer::getPassword);

        // Désactive les tâches planifiées de l'application pendant les tests pour éviter les interférences.
        registry.add("spring.task.scheduling.enabled", () -> "false");

        // Bonne pratique JPA/Hibernate : éviter les "N+1 selects" et les LazyInitializationException
        // en désactivant l'Open-In-View en test.
        registry.add("spring.jpa.open-in-view", () -> "false");
    }
}
