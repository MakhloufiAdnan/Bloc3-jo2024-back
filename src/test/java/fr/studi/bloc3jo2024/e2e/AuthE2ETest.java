package fr.studi.bloc3jo2024.e2e;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.integration.AbstractIntegrationTest;
import jakarta.mail.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AuthE2ETest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthE2ETest.class);
    private static final Pattern CONFIRMATION_TOKEN_PATTERN = Pattern.compile("\\?token=([a-zA-Z0-9\\-]+)");
    private static final Duration EMAIL_RETRIEVAL_TIMEOUT = Duration.ofSeconds(45);
    private static final Duration EMAIL_POLL_INTERVAL = Duration.ofSeconds(5);
    private static final String CONFIRMATION_EMAIL_SUBJECT_PART = "Confirmation de votre compte";

    private static WebTestClient webTestClient;

    @BeforeAll
    static void setUpClient() {
        String baseUrl = AbstractIntegrationTest.getBackendBaseUrl();
        log.info("AuthE2ETest.setUpClient - Initialisation de WebTestClient avec baseUrl: {}", baseUrl);
        webTestClient = WebTestClient.bindToServer().baseUrl(baseUrl).build();
    }

    private RegisterRequestDto createSampleRegisterDto(String email, String username, String password, String firstname, LocalDate birthDate, String country) {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername(username);
        dto.setFirstname(firstname);
        dto.setDate(birthDate);
        dto.setEmail(email);
        dto.setPhonenumber("0601020304");
        dto.setStreetnumber(123);
        dto.setAddress("Main St");
        dto.setPostalcode("75001");
        dto.setCity("Paris");
        dto.setPassword(password);
        dto.setCountry(country);
        return dto;
    }

    private record EmailDetails(String subject, String content) {}

    private EmailDetails retrieveAndFindEmail(String host, int port, String userEmailAccount) {
        Store store = null;
        Folder inbox = null;
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imap");

            Session emailSession = Session.getInstance(props);
            store = emailSession.getStore("imap");
            store.connect(host, port, userEmailAccount, userEmailAccount);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            log.debug("Tentative de récupération : {} email(s) trouvé(s) pour {} via IMAP {}:{}.",
                    (messages != null ? messages.length : 0), userEmailAccount, host, port);

            if (messages != null) {
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message msg = messages[i];
                    String subject = msg.getSubject();
                    if (subject != null && subject.contains(CONFIRMATION_EMAIL_SUBJECT_PART)) {
                        log.info("Email correspondant trouvé pour {} avec sujet : '{}'", userEmailAccount, subject);
                        Object contentObj = msg.getContent();
                        String emailBody;
                        if (contentObj instanceof String) {
                            emailBody = (String) contentObj;
                        } else if (contentObj instanceof Multipart multipart) {
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart bodyPart = multipart.getBodyPart(j);
                                if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                                    sb.append(bodyPart.getContent().toString());
                                }
                            }
                            emailBody = sb.toString();
                        } else {
                            log.warn("Type de contenu d'email non géré (Content-Type: {}) pour {}. Sujet: {}",
                                    msg.getContentType(), userEmailAccount, subject);
                            continue; // Passe à l'email suivant
                        }
                        return new EmailDetails(subject, emailBody);
                    }
                }
            }
            return null; // Aucun email correspondant trouvé
        } catch (MessagingException | IOException e) {
            log.warn("Erreur lors de la récupération ou du traitement des emails pour {}: {}", userEmailAccount, e.getMessage(), e);
            return null;
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false); // false pour ne pas supprimer les messages
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException e) { // Renommé de "ignored" à "e"
                log.trace("Exception (généralement ignorée ici) lors de la fermeture des ressources IMAP.", e);
            }
        }
    }

    private String extractTokenFromEmailBody(String emailBody) {
        if (emailBody == null || emailBody.trim().isEmpty()) {
            log.warn("Tentative d'extraction de token à partir d'un corps d'email vide ou null.");
            return null;
        }
        Matcher matcher = CONFIRMATION_TOKEN_PATTERN.matcher(emailBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("Token de confirmation non trouvé dans le corps de l'email. Analysé (max 500 char) : \"{}\"",
                emailBody.substring(0, Math.min(emailBody.length(), 500)) + (emailBody.length() > 500 ? "..." : ""));
        return null;
    }

    @Test
    void testFullRegistrationLoginAndAccessProtectedResource() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String userEmail = "fullflow_" + uniqueSuffix + "@example.com";
        String username = "user_ff_" + uniqueSuffix;
        String userPassword = "PasswordFullFlow123!";
        RegisterRequestDto registerDto = createSampleRegisterDto(userEmail, username, userPassword, "FullFlowUser", LocalDate.of(1992, 3, 15), "Canada");

        log.info("1. Inscription de l'utilisateur : {}", userEmail);
        webTestClient.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthReponseDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Inscription réussie. Un email de confirmation a été envoyé.");
                });
        log.info("Demande d'inscription (201 Created) pour {} réussie.", userEmail);

        log.info("2. Attente et récupération de l'email de confirmation pour : {}", userEmail);
        Callable<EmailDetails> emailFinderCallable = () -> retrieveAndFindEmail(
                AbstractIntegrationTest.getGreenMailImapHost(),
                AbstractIntegrationTest.getGreenMailImapMappedPort(),
                userEmail // Le sujet attendu est maintenant géré dans la méthode retrieveAndFindEmail
        );

        EmailDetails confirmationEmailDetails = await()
                .pollInSameThread()
                .atMost(EMAIL_RETRIEVAL_TIMEOUT)
                .pollInterval(EMAIL_POLL_INTERVAL)
                .ignoreExceptions() // Pour les tentatives de connexion IMAP qui pourraient échouer temporairement
                .until(emailFinderCallable, Objects::nonNull);

        assertThat(confirmationEmailDetails).as("Les détails de l'email de confirmation pour %s ne devraient pas être null", userEmail).isNotNull();
        log.info("Email de confirmation trouvé pour {}. Sujet: '{}'", userEmail, confirmationEmailDetails.subject());

        String confirmationToken = extractTokenFromEmailBody(confirmationEmailDetails.content());
        assertThat(confirmationToken).as("Token de confirmation non trouvé dans l'email pour " + userEmail).isNotNull().isNotBlank();
        log.info("Token de confirmation extrait pour {}: {}", userEmail, confirmationToken);

        log.info("3. Confirmation du compte pour : {} avec le token", userEmail);
        webTestClient.get().uri("/api/auth/confirm?token=" + confirmationToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .isEqualTo("Compte activé. Vous pouvez désormais vous connecter.");
        log.info("Confirmation du compte réussie pour : {}", userEmail);

        LoginUtilisateurRequestDto loginDto = new LoginUtilisateurRequestDto();
        loginDto.setEmail(userEmail);
        loginDto.setPassword(userPassword);

        log.info("4. Tentative de connexion pour : {}", userEmail);
        AuthReponseDto authResponse = webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(AuthReponseDto.class)
                .returnResult().getResponseBody();

        assertThat(authResponse).as("AuthReponseDto ne devrait pas être null après login pour " + userEmail).isNotNull();
        assertThat(authResponse.getToken()).as("Token JWT ne devrait pas être null ou vide pour " + userEmail).isNotNull().isNotBlank();
        String jwtToken = authResponse.getToken();
        log.info("Connexion réussie pour : {}. Token JWT acquis.", userEmail);

        log.info("5. Tentative d'accès à la ressource protégée /api/test/secured pour : {}", userEmail);
        webTestClient.get().uri("/api/test/secured")
                .header("Authorization", "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Accès à l'endpoint sécurisé (/api/test/secured) réussi !");
        log.info("Accès à la ressource protégée réussi pour : {}", userEmail);
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "dup_" + uniqueSuffix + "@example.com";
        String password = "SecureTestPassword123!";
        String username1 = "user1_dup_" + uniqueSuffix;
        String username2 = "user2_dup_" + uniqueSuffix;

        RegisterRequestDto dto1 = createSampleRegisterDto(email, username1, password, "UserOne", LocalDate.of(1990, 1, 1), "France");
        RegisterRequestDto dto2 = createSampleRegisterDto(email, username2, password, "UserTwo", LocalDate.of(1990, 1, 1), "France");

        log.info("Test d'inscription avec email déjà existant : {}", email);

        webTestClient.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto1)
                .exchange()
                .expectStatus().isCreated();
        log.info("Première inscription (201 Created) pour l'email dupliqué {} effectuée.", email);

        webTestClient.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto2)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(AuthReponseDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Email déjà utilisé.");
                });
        log.info("Test terminé : tentative d'inscription avec email dupliqué {} correctement rejetée (400 Bad Request).", email);
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        LoginUtilisateurRequestDto loginDto = new LoginUtilisateurRequestDto();
        loginDto.setEmail("nonexistent_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        loginDto.setPassword("WrongPassword123!");

        webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDto)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(AuthReponseDto.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getToken()).isNull(); // Pas de token en cas d'échec
                    assertThat(response.getMessage()).isEqualTo("Email ou mot de passe invalide.");
                });
        log.info("Test de connexion avec identifiants invalides terminé (401 Unauthorized attendu).");
    }

    @Test
    void testAccessProtectedResource_WithoutToken() {
        webTestClient.get().uri("/api/test/secured") // Adaptez si nécessaire
                .exchange()
                .expectStatus().isUnauthorized();
        log.info("Test d'accès à une ressource protégée sans token terminé (401 Unauthorized attendu).");
    }
}