package fr.studi.bloc3jo2024.integration;

import org.junit.jupiter.api.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers; // Assurez-vous que cette annotation est toujours là si vous l'utilisez pour la gestion du cycle de vie JUnit 5
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

@Testcontainers // Gardez cette annotation si vos tests d'intégration l'utilisent pour le cycle de vie
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    // Déclarez vos constantes ici, au niveau de la classe
    public static final int GREENMAIL_SMTP_INTERNAL_PORT = 3025;
    public static final int GREENMAIL_IMAP_INTERNAL_PORT = 3143;
    private static final DockerImageName GREENMAIL_IMAGE = DockerImageName.parse("greenmail/standalone:2.1.0");
    private static final String GREENMAIL_NETWORK_ALIAS = "greenmail-e2e-service";

    private static final String BACKEND_NETWORK_ALIAS = "backend-e2e-service";
    private static final int BACKEND_INTERNAL_PORT = 8080; // <--- ASSUREZ-VOUS QUE CETTE LIGNE EST PRÉSENTE ET CORRECTE
    private static final String BACKEND_SPRING_PROFILE = "dev"; // Ou le profil dédié à vos tests E2E
    private static final String ACTUATOR_HEALTH_ENDPOINT_PATH = "/management/health";

    private static final String DB_NETWORK_ALIAS = "db-e2e-service";

    public static Network sharedNetwork = Network.newNetwork();

    @SuppressWarnings("resource")
    public static GenericContainer<?> greenMailContainer;
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> postgresDBContainer;
    @SuppressWarnings("resource")
    public static GenericContainer<?> backendContainer;

    private static final Properties envFileProperties = new Properties();

    static {
        // Condition pour exécuter ce bloc uniquement si la propriété système est à true
        // Cette propriété sera mise à true par maven-failsafe-plugin
        if ("true".equals(System.getProperty("run.integration.tests"))) {
            log.info("=== Static Initializer: Initializing and starting Testcontainers for Integration/E2E ===");
            loadEnvPropertiesFromFile(); // Charger .env seulement si on démarre les conteneurs

            try {
                log.info("Starting GreenMail container ({}) using alias '{}'...", GREENMAIL_IMAGE, GREENMAIL_NETWORK_ALIAS);
                greenMailContainer = new GenericContainer<>(GREENMAIL_IMAGE)
                        .withExposedPorts(GREENMAIL_SMTP_INTERNAL_PORT, GREENMAIL_IMAP_INTERNAL_PORT)
                        .withNetwork(sharedNetwork)
                        .withNetworkAliases(GREENMAIL_NETWORK_ALIAS)
                        .waitingFor(Wait.forListeningPorts(GREENMAIL_SMTP_INTERNAL_PORT, GREENMAIL_IMAP_INTERNAL_PORT)
                                .withStartupTimeout(Duration.ofMinutes(2)))
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("docker-greenmail")).withPrefix("GREENMAIL"));
                greenMailContainer.start();
                log.info("GreenMail container started. Mapped ports - SMTP: {}, IMAP: {}",
                        greenMailContainer.getMappedPort(GREENMAIL_SMTP_INTERNAL_PORT),
                        greenMailContainer.getMappedPort(GREENMAIL_IMAP_INTERNAL_PORT));

                String dbName = envFileProperties.getProperty("POSTGRES_DB", "jo2024_db");
                String dbUser = envFileProperties.getProperty("POSTGRES_USER", "postgres");
                String dbPassword = envFileProperties.getProperty("POSTGRES_PASSWORD", "password");

                log.info("Starting PostgreSQL container (postgres:17-alpine3.21) for db '{}' with user '{}' using alias '{}'...", dbName, dbUser, DB_NETWORK_ALIAS);
                postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine3.21")
                        .withDatabaseName(dbName)
                        .withUsername(dbUser)
                        .withPassword(dbPassword)
                        .withNetwork(sharedNetwork)
                        .withNetworkAliases(DB_NETWORK_ALIAS)
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("docker-db")).withPrefix("DB"))
                        .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
                postgresDBContainer.start();
                log.info("PostgreSQL container started. JDBC URL for backend (internal Docker network): jdbc:postgresql://{}:{}/{}",
                        DB_NETWORK_ALIAS, 5432, postgresDBContainer.getDatabaseName());

                log.info("Starting Backend container (building image from Dockerfile) using alias '{}'...", BACKEND_NETWORK_ALIAS);
                File dockerfile = new File("Dockerfile").getAbsoluteFile();
                if (!dockerfile.exists()) {
                    throw new IllegalStateException("Dockerfile not found at resolved absolute path: " + dockerfile.getAbsolutePath());
                }
                File dockerBuildContext = dockerfile.getParentFile();
                if (dockerBuildContext == null || !dockerBuildContext.exists() || !dockerBuildContext.isDirectory()) {
                    throw new IllegalStateException("Invalid Docker build context derived from Dockerfile path: " +
                            (dockerBuildContext != null ? dockerBuildContext.getAbsolutePath() : "null"));
                }
                log.info("Using Docker build context: {}", dockerBuildContext.getAbsolutePath());
                log.info("Using Dockerfile for backend image: {}", dockerfile.getAbsolutePath());

                backendContainer = new GenericContainer<>(
                        new ImageFromDockerfile("bloc3jo2024-backend-e2e-image", false)
                                .withDockerfile(dockerfile.toPath())
                )
                        .dependsOn(postgresDBContainer, greenMailContainer)
                        .withNetwork(sharedNetwork)
                        .withNetworkAliases(BACKEND_NETWORK_ALIAS)
                        .withExposedPorts(BACKEND_INTERNAL_PORT) // Utilisation de la constante
                        .withEnv("SPRING_PROFILES_ACTIVE", BACKEND_SPRING_PROFILE)
                        .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + DB_NETWORK_ALIAS + ":5432/" + postgresDBContainer.getDatabaseName())
                        .withEnv("SPRING_DATASOURCE_USERNAME", postgresDBContainer.getUsername())
                        .withEnv("SPRING_DATASOURCE_PASSWORD", postgresDBContainer.getPassword())
                        .withEnv("SPRING_JPA_HIBERNATE_DDL_AUTO", "create-drop") // Important pour le comportement de l'app dans le conteneur
                        .withEnv("SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION", "true")
                        .withEnv("SPRING_SQL_INIT_MODE", "always")
                        .withEnv("SPRING_MAIL_HOST", "host.docker.internal") // Pour que le conteneur backend atteigne Greenmail sur l'hôte Docker
                        .withEnv("SPRING_MAIL_PORT", String.valueOf(greenMailContainer.getMappedPort(GREENMAIL_SMTP_INTERNAL_PORT)))
                        // Ajoutez ici TOUTES les autres variables d'environnement nécessaires à votre backend
                        .withEnv("JWT_SECRET", envFileProperties.getProperty("JWT_SECRET", "default-e2e-jwt-secret-super-long-and-secure-0123456789abcdef"))
                        // ... (autres .withEnv() basées sur envFileProperties ou des valeurs fixes) ...
                        .withEnv("PORT", String.valueOf(BACKEND_INTERNAL_PORT)) // Le port interne du conteneur backend
                        .waitingFor(Wait.forHttp(ACTUATOR_HEALTH_ENDPOINT_PATH)
                                .forPort(BACKEND_INTERNAL_PORT) // Vérifie le port interne
                                .forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("docker-backend")).withPrefix("BACKEND"));
                backendContainer.start();
                log.info("Backend container started. Mapped port for WebTestClient access: {}. Base URL: {}",
                        backendContainer.getMappedPort(BACKEND_INTERNAL_PORT), getBackendBaseUrl());
                log.info("Testcontainers environment fully started and ready for tests.");

            } catch (Exception e) {
                log.error("FATAL error during Testcontainers static initialization. Attempting to stop any running containers.", e);
                // ... (votre logique d'arrêt d'urgence des conteneurs) ...
                throw new RuntimeException("Failed to start Testcontainers environment due to: " + e.getMessage(), e);
            }
        } else {
            log.warn("=== Static Initializer: SKIPPING Testcontainers initialization (System property 'run.integration.tests' is not 'true') ===");
        }
    }

    @AfterAll
    static void stopAllContainers() {
        // Condition pour exécuter ce bloc uniquement si les conteneurs ont été démarrés
        if ("true".equals(System.getProperty("run.integration.tests"))) {
            log.info("=== @AfterAll: Shutting down Testcontainers environment ===");
            // ... (votre logique d'arrêt des conteneurs : backendContainer.stop(), greenMailContainer.stop(), etc.) ...
            if (backendContainer != null && backendContainer.isRunning()) try { backendContainer.stop(); log.info("Backend container stopped."); } catch (Exception ex) {log.warn("Error stopping backend container during error handling.", ex); }
            if (greenMailContainer != null && greenMailContainer.isRunning()) try { greenMailContainer.stop(); log.info("GreenMail container stopped."); } catch (Exception ex) { log.warn("Error stopping GreenMail container during error handling.", ex); }
            if (postgresDBContainer != null && postgresDBContainer.isRunning()) try { postgresDBContainer.stop(); log.info("PostgreSQL container stopped."); } catch (Exception ex) { log.warn("Error stopping PostgreSQL container during error handling.", ex); }
            if (sharedNetwork != null) try { sharedNetwork.close(); log.info("Shared Docker network closed.");} catch (Exception ex) { log.warn("Error closing shared Docker network during error handling.", ex); }
            log.info("Testcontainers environment shutdown complete.");
        } else {
            log.info("=== @AfterAll: SKIPPING Testcontainers shutdown (not an E2E test run or containers not started) ===");
        }
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // Condition pour exécuter ce bloc uniquement si les conteneurs ont été démarrés
        if ("true".equals(System.getProperty("run.integration.tests"))) {
            log.info("=== @DynamicPropertySource: Registering dynamic properties for Spring context (E2E Tests) ===");
            if (backendContainer != null && backendContainer.isRunning()) {
                registry.add("app.backend.base-url", AbstractIntegrationTest::getBackendBaseUrl);
                log.info("Registered 'app.backend.base-url' for test client: {}", getBackendBaseUrl());
            } else {
                // Si run.integration.tests est true, les conteneurs devraient être démarrés.
                // Une erreur ici pourrait indiquer un problème dans le bloc static.
                log.error("CRITICAL (E2E): Backend container is NOT running when @DynamicPropertySource is called, but integration tests are expected to run.");
            }

            if (greenMailContainer != null && greenMailContainer.isRunning()) {
                registry.add("test.greenmail.host", greenMailContainer::getHost);
                registry.add("test.greenmail.smtp.port", () -> greenMailContainer.getMappedPort(GREENMAIL_SMTP_INTERNAL_PORT));
                registry.add("test.greenmail.imap.port", () -> greenMailContainer.getMappedPort(GREENMAIL_IMAP_INTERNAL_PORT));
                log.info("Registered GreenMail E2E test properties: host={}, smtp.port={}, imap.port={}",
                        greenMailContainer.getHost(),
                        greenMailContainer.getMappedPort(GREENMAIL_SMTP_INTERNAL_PORT),
                        greenMailContainer.getMappedPort(GREENMAIL_IMAP_INTERNAL_PORT));
            } else {
                log.error("CRITICAL (E2E): GreenMail container is NOT running during @DynamicPropertySource, but integration tests are expected to run.");
            }
        } else {
            log.info("=== @DynamicPropertySource: SKIPPING dynamic properties (not an E2E test run or containers not started) ===");
        }
    }

    private static void loadEnvPropertiesFromFile() {
        File envFile = new File(".env").getAbsoluteFile();
        if (!envFile.exists()) {
            log.warn(".env file not found at: {}. Using hardcoded default test values for E2E configuration.", envFile.getAbsolutePath());
            envFileProperties.setProperty("POSTGRES_DB", "jo2024_e2e_default");
            // ... (vos autres valeurs par défaut) ...
            return;
        }
        log.info("Loading E2E environment properties from .env file: {}", envFile.getAbsolutePath());
        try (FileReader reader = new FileReader(envFile)) {
            envFileProperties.load(reader);
            log.info(".env file loaded successfully.");
        } catch (IOException e) {
            log.error("Error loading .env file at {}. Proceeding with defaults if any.", envFile.getAbsolutePath(), e);
        }
    }

    public static String getBackendBaseUrl() {
        if (!"true".equals(System.getProperty("run.integration.tests"))) {
            // Pour les tests unitaires, ne pas dépendre de ce conteneur
            return "http://localhost:8080"; // Ou une valeur appropriée pour les tests non-E2E
        }
        if (backendContainer == null || !backendContainer.isRunning()) {
            log.error("Attempted to get backend base URL for E2E test, but backend container is not running or not initialized.");
            throw new IllegalStateException("Testcontainers (Backend container) not initialized or not running for an E2E test. Check static initializer block for errors.");
        }
        return String.format("http://%s:%d", backendContainer.getHost(), backendContainer.getMappedPort(BACKEND_INTERNAL_PORT));
    }

    // Méthodes similaires pour GreenMail si nécessaire, rendues conditionnelles aussi
    public static String getGreenMailImapHost() {
        if (!"true".equals(System.getProperty("run.integration.tests")) || greenMailContainer == null || !greenMailContainer.isRunning()) {
            return "localhost"; // Valeur par défaut si non pertinent
        }
        return greenMailContainer.getHost();
    }

    public static Integer getGreenMailImapMappedPort() {
        if (!"true".equals(System.getProperty("run.integration.tests")) || greenMailContainer == null || !greenMailContainer.isRunning()) {
            return 0; // Valeur par défaut
        }
        return greenMailContainer.getMappedPort(GREENMAIL_IMAP_INTERNAL_PORT);
    }
}