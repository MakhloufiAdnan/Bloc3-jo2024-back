/*package fr.studi.bloc3jo2024.integration;

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
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    public static final int GREENMAIL_SMTP_INTERNAL_PORT = 3025;
    public static final int GREENMAIL_IMAP_INTERNAL_PORT = 3143;
    private static final DockerImageName GREENMAIL_IMAGE = DockerImageName.parse("greenmail/standalone:2.1.0");
    private static final String GREENMAIL_NETWORK_ALIAS = "greenmail-e2e-service";

    private static final String BACKEND_NETWORK_ALIAS = "backend-e2e-service";
    private static final int BACKEND_INTERNAL_PORT = 8080;
    private static final String BACKEND_SPRING_PROFILE = "dev";
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
        log.info("=== Static Initializer: Loading .env properties ===");
        loadEnvPropertiesFromFile();

        log.info("=== Static Initializer: Initializing and starting Testcontainers ===");
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

            log.info("Starting PostgreSQL container (postgres:17-alpine) for db '{}' with user '{}' using alias '{}'...", dbName, dbUser, DB_NETWORK_ALIAS);
            postgresDBContainer = new PostgreSQLContainer<>("postgres:17-alpine")
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
                    .withExposedPorts(BACKEND_INTERNAL_PORT)
                    .withEnv("SPRING_PROFILES_ACTIVE", BACKEND_SPRING_PROFILE)
                    .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + DB_NETWORK_ALIAS + ":5432/" + postgresDBContainer.getDatabaseName())
                    .withEnv("SPRING_DATASOURCE_USERNAME", postgresDBContainer.getUsername())
                    .withEnv("SPRING_DATASOURCE_PASSWORD", postgresDBContainer.getPassword())
                    .withEnv("SPRING_JPA_HIBERNATE_DDL_AUTO", "create-drop")
                    .withEnv("SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION", "true")
                    .withEnv("SPRING_SQL_INIT_MODE", "always") // Assure l'exécution de data.sql
                    .withEnv("SPRING_MAIL_HOST", "host.docker.internal")
                    .withEnv("SPRING_MAIL_PORT", String.valueOf(greenMailContainer.getMappedPort(GREENMAIL_SMTP_INTERNAL_PORT)))
                    .withEnv("SPRING_MAIL_USERNAME", envFileProperties.getProperty("EMAIL_USERNAME", ""))
                    .withEnv("SPRING_MAIL_PASSWORD", envFileProperties.getProperty("EMAIL_PASSWORD", ""))
                    .withEnv("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH", "false")
                    .withEnv("SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE", "false")
                    .withEnv("MANAGEMENT_HEALTH_MAIL_ENABLED", "false")
                    .withEnv("JWT_SECRET", envFileProperties.getProperty("JWT_SECRET", "default-e2e-jwt-secret-super-long-and-secure-0123456789abcdef"))
                    .withEnv("JWT_EXPIRATION", envFileProperties.getProperty("JWT_EXPIRATION", "3600000"))
                    .withEnv("ADMIN_EMAIL", envFileProperties.getProperty("ADMIN_EMAIL", "admin-e2e-default@example.com"))
                    .withEnv("ADMIN_PASSWORD", envFileProperties.getProperty("ADMIN_PASSWORD", "AdminE2EDefaultPass123!"))
                    .withEnv("FRONTEND_URL_CONF", envFileProperties.getProperty("FRONTEND_URL_CONF_DOCKER", "http://e2e.frontend.test/confirm"))
                    .withEnv("FRONTEND_URL_RESET", envFileProperties.getProperty("FRONTEND_URL_RESET_DOCKER", "http://e2e.frontend.test/reset"))
                    .withEnv("ID_GOOGLE", envFileProperties.getProperty("ID_GOOGLE", ""))
                    .withEnv("MDP_GOOGLE", envFileProperties.getProperty("MDP_GOOGLE", ""))
                    .withEnv("PORT", String.valueOf(BACKEND_INTERNAL_PORT))
                    .waitingFor(Wait.forHttp(ACTUATOR_HEALTH_ENDPOINT_PATH)
                            .forPort(BACKEND_INTERNAL_PORT)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(5)))
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("docker-backend")).withPrefix("BACKEND"));
            backendContainer.start();
            log.info("Backend container started. Mapped port for WebTestClient access: {}. Base URL: {}",
                    backendContainer.getMappedPort(BACKEND_INTERNAL_PORT), getBackendBaseUrl());
            log.info("Testcontainers environment fully started and ready for tests.");

        } catch (Exception e) {
            log.error("FATAL error during Testcontainers static initialization. Attempting to stop any running containers.", e);
            if (backendContainer != null && backendContainer.isRunning()) try { backendContainer.stop(); } catch (Exception ex) {log.warn("Error stopping backend container during error handling.", ex); }
            if (greenMailContainer != null && greenMailContainer.isRunning()) try { greenMailContainer.stop(); } catch (Exception ex) { log.warn("Error stopping GreenMail container during error handling.", ex); }
            if (postgresDBContainer != null && postgresDBContainer.isRunning()) try { postgresDBContainer.stop(); } catch (Exception ex) { log.warn("Error stopping PostgreSQL container during error handling.", ex); }
            if (sharedNetwork != null) try { sharedNetwork.close(); } catch (Exception ex) { log.warn("Error closing shared Docker network during error handling.", ex); }
            throw new RuntimeException("Failed to start Testcontainers environment due to: " + e.getMessage(), e);
        }
    }

    @AfterAll
    static void stopAllContainers() {
        log.info("=== @AfterAll: Shutting down Testcontainers environment ===");
        if (backendContainer != null && backendContainer.isRunning()) {
            try { backendContainer.stop(); log.info("Backend container stopped."); }
            catch (Exception e) { log.warn("Error stopping backend container.", e); }
        }
        if (greenMailContainer != null && greenMailContainer.isRunning()) {
            try { greenMailContainer.stop(); log.info("GreenMail container stopped."); }
            catch (Exception e) { log.warn("Error stopping GreenMail container.", e); }
        }
        if (postgresDBContainer != null && postgresDBContainer.isRunning()) {
            try { postgresDBContainer.stop(); log.info("PostgreSQL container stopped."); }
            catch (Exception e) { log.warn("Error stopping PostgreSQL container.", e); }
        }
        if (sharedNetwork != null) {
            try { sharedNetwork.close(); log.info("Shared Docker network closed."); }
            catch (Exception e) { log.warn("Error closing shared Docker network.", e); }
        }
        log.info("Testcontainers environment shutdown complete.");
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        log.info("=== @DynamicPropertySource: Registering dynamic properties for Spring context (E2E Tests) ===");
        if (backendContainer != null && backendContainer.isRunning()) {
            registry.add("app.backend.base-url", AbstractIntegrationTest::getBackendBaseUrl);
            log.info("Registered 'app.backend.base-url' for test client: {}", getBackendBaseUrl());
        } else {
            log.error("CRITICAL (E2E): Backend container is NOT running when @DynamicPropertySource is called.");
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
            log.error("CRITICAL (E2E): GreenMail container is NOT running during @DynamicPropertySource.");
        }
    }

    private static void loadEnvPropertiesFromFile() {
        File envFile = new File(".env").getAbsoluteFile();
        if (!envFile.exists()) {
            log.warn(".env file not found at: {}. Using hardcoded default test values for E2E configuration.", envFile.getAbsolutePath());
            envFileProperties.setProperty("POSTGRES_DB", "jo2024_e2e_default");
            envFileProperties.setProperty("POSTGRES_USER", "user_e2e_default");
            envFileProperties.setProperty("POSTGRES_PASSWORD", "pass_e2e_default");
            envFileProperties.setProperty("JWT_EXPIRATION", "3600000");
            // Ajoutez d'autres valeurs par défaut nécessaires ici
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
        if (backendContainer == null || !backendContainer.isRunning()) {
            log.error("Attempted to get backend base URL, but backend container is not running or not initialized.");
            throw new IllegalStateException("Testcontainers (Backend container) not initialized or not running. Check static initializer block for errors.");
        }
        return String.format("http://%s:%d", backendContainer.getHost(), backendContainer.getMappedPort(BACKEND_INTERNAL_PORT));
    }

    public static String getGreenMailImapHost() {
        if (greenMailContainer == null || !greenMailContainer.isRunning()) {
            log.error("Attempted to get GreenMail IMAP host, but GreenMail container is not running or not initialized.");
            throw new IllegalStateException("Testcontainers (GreenMail container) not initialized or not running.");
        }
        return greenMailContainer.getHost();
    }

    public static Integer getGreenMailImapMappedPort() {
        if (greenMailContainer == null || !greenMailContainer.isRunning()) {
            log.error("Attempted to get GreenMail IMAP mapped port, but GreenMail container is not running or not initialized.");
            throw new IllegalStateException("Testcontainers (GreenMail container) not initialized or not running.");
        }
        return greenMailContainer.getMappedPort(GREENMAIL_IMAP_INTERNAL_PORT);
    }
}*/