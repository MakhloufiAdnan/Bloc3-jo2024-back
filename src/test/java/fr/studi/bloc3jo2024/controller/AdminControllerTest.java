package fr.studi.bloc3jo2024.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.dto.authentification.LoginAdminRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper objectMapper;

    private final String correctAdminEmail = "admin-local@example.com";
    private final String correctAdminPasswordPlainText = "AdminLocalPass123!";
    private final String configuredAdminPasswordHashInController = "le_hash_attendu_pour_AdminLocalPass123!";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminController, "configuredAdminEmail", correctAdminEmail);
        ReflectionTestUtils.setField(adminController, "configuredAdminPasswordHash", configuredAdminPasswordHashInController);
    }

    @Test
    void adminLogin_validCredentials_returnsOkAndSetsSession() {
        // Arrange : Préparation du test
        LoginAdminRequestDto credentials = new LoginAdminRequestDto();
        credentials.setEmail(correctAdminEmail);
        credentials.setPassword(correctAdminPasswordPlainText);

        // Simule le comportement de request.getSession(true) pour retourner notre session mockée.
        when(request.getSession(true)).thenReturn(session);
        // Simule une correspondance de mot de passe réussie par PasswordEncoder.
        when(passwordEncoder.matches(correctAdminPasswordPlainText, configuredAdminPasswordHashInController)).thenReturn(true);

        // Act : Exécution de la méthode à tester
        ResponseEntity<Map<String, String>> response = adminController.adminLogin(credentials, request);

        // Assert : Vérification des résultats
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Le statut HTTP doit être OK pour une connexion réussie.");
        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null.");
        assertEquals("admin-session-active", response.getBody().get("token"), "Le token de session est incorrect.");
        assertEquals("Connexion administrateur réussie.", response.getBody().get("message"), "Le message de succès est incorrect.");

        // Vérifie que l'attribut de session a été correctement positionné.
        verify(session, times(1)).setAttribute(AdminController.SESSION_ADMIN_LOGGED_IN, true);
    }

    @Test
    void adminLogin_invalidEmail_returnsUnauthorized() {
        // Arrange
        LoginAdminRequestDto credentials = new LoginAdminRequestDto();
        credentials.setEmail("wrong@admin.com"); // Email incorrect
        credentials.setPassword(correctAdminPasswordPlainText);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogin(credentials, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Le statut HTTP doit être Unauthorized pour un email invalide.");
        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null.");
        assertEquals("Identifiants administrateur invalides.", response.getBody().get("message"), "Le message d'erreur est incorrect.");

        // Vérifie que la session n'a pas été créée ou modifiée.
        verify(request, never()).getSession(true);
        verify(session, never()).setAttribute(anyString(), any());
        // Vérifie que la comparaison de mot de passe n'a pas été tentée (car l'email était déjà faux).
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void adminLogin_incorrectPassword_returnsUnauthorized() {
        // Arrange
        LoginAdminRequestDto credentials = new LoginAdminRequestDto();
        credentials.setEmail(correctAdminEmail);
        credentials.setPassword("wrongpassword"); // Mot de passe incorrect

        when(passwordEncoder.matches("wrongpassword", configuredAdminPasswordHashInController)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogin(credentials, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Le statut HTTP doit être Unauthorized pour un mot de passe incorrect.");
        assertNotNull(response.getBody(), "Le corps de la réponse ne doit pas être null.");
        assertEquals("Identifiants administrateur invalides.", response.getBody().get("message"), "Le message d'erreur est incorrect.");

        // Vérifie que passwordEncoder.matches a bien été appelé avec les bonnes valeurs.
        verify(passwordEncoder, times(1)).matches("wrongpassword", configuredAdminPasswordHashInController);
        // Vérifie que la session n'a pas été créée ou modifiée.
        verify(request, never()).getSession(true);
        verify(session, never()).setAttribute(anyString(), any());
    }


    @Test
    void checkAdminSession_sessionExistsAndAdminLoggedIn_returnsTrue() {
        // Arrange
        when(request.getSession(false)).thenReturn(session); // Session existante
        when(session.getAttribute(AdminController.SESSION_ADMIN_LOGGED_IN)).thenReturn(true); // Admin connecté

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("authenticated"), "L'administrateur devrait être authentifié.");
    }

    @Test
    void checkAdminSession_sessionExistsButAdminNotLoggedIn_returnsFalse() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AdminController.SESSION_ADMIN_LOGGED_IN)).thenReturn(null); // ou false

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("authenticated"), "L'administrateur ne devrait pas être authentifié.");
    }

    @Test
    void checkAdminSession_sessionDoesNotExist_returnsFalse() {
        // Arrange
        when(request.getSession(false)).thenReturn(null); // Pas de session

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("authenticated"), "L'administrateur ne devrait pas être authentifié si la session n'existe pas.");
    }

    @Test
    void adminLogout_sessionExists_invalidatesSessionAndReturnsOk() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Déconnexion administrateur réussie. Session invalidée.", response.getBody().get("message"));
        verify(session, times(1)).invalidate(); // Vérifie que la session a été invalidée.
    }

    @Test
    void adminLogout_sessionDoesNotExist_returnsOkWithMessage() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Aucune session administrateur active à déconnecter.", response.getBody().get("message"));
        verify(session, never()).invalidate(); // Vérifie que invalidate n'a pas été appelé.
    }

    @Test
    void adminDashboard_adminLoggedIn_returnsOkAndWelcomeMessage() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AdminController.SESSION_ADMIN_LOGGED_IN)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bienvenue sur le dashboard administrateur.", response.getBody().get("message"));
    }

    @Test
    void adminDashboard_adminNotLoggedIn_returnsUnauthorized() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AdminController.SESSION_ADMIN_LOGGED_IN)).thenReturn(null); // Admin non connecté

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals("Non autorisé. Veuillez vous connecter.", response.getBody().get("message"));
    }

    @Test
    void adminDashboard_sessionDoesNotExist_returnsUnauthorized() {
        // Arrange
        when(request.getSession(false)).thenReturn(null); // Pas de session

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals("Non autorisé. Veuillez vous connecter.", response.getBody().get("message"));
    }
}