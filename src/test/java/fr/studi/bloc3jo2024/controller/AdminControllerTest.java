package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.authentification.LoginAdminRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private AutoCloseable closeable;

    private final String correctAdminEmail = "test@admin.com";
    private final String correctAdminPassword = "motdepasse123";

    @BeforeEach
    void setUp() {
        // Arrange : Initialiser les mocks et injecter les valeurs du contrôleur
        closeable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(adminController, "adminEmail", correctAdminEmail);
        ReflectionTestUtils.setField(adminController, "adminPassword", correctAdminPassword);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Libère les ressources des mocks
        if (closeable != null) closeable.close();
    }

    @Test
    void adminLogin_validCredentials_returnsOkAndSetsSession() {
        // Arrange
        LoginAdminRequestDto credentials = new LoginAdminRequestDto();
        credentials.setEmail(correctAdminEmail);
        credentials.setPassword(correctAdminPassword);
        when(request.getSession()).thenReturn(session);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogin(credentials, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("admin-session", response.getBody().get("token"));
        assertEquals("Connexion administrateur réussie.", response.getBody().get("message"));
        verify(session, times(1)).setAttribute("ADMIN_LOGGED_IN", true);
    }

    @Test
    void adminLogin_invalidCredentials_returnsUnauthorized() {
        // Arrange
        LoginAdminRequestDto credentials = new LoginAdminRequestDto();
        credentials.setEmail("wrong@admin.com");
        credentials.setPassword("wrongpassword");

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogin(credentials, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Identifiants administrateur invalides.", response.getBody().get("message"));
        verify(request, never()).getSession();
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void checkAdminSession_sessionExistsAndAdminLoggedIn_returnsTrue() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("ADMIN_LOGGED_IN")).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("authenticated"));
    }

    @Test
    void checkAdminSession_sessionExistsButAdminNotLoggedIn_returnsFalse() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("ADMIN_LOGGED_IN")).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("authenticated"));
    }

    @Test
    void checkAdminSession_sessionDoesNotExist_returnsFalse() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Boolean>> response = adminController.checkAdminSession(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("authenticated"));
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
        assertEquals("Déconnexion administrateur réussie.", response.getBody().get("message"));
        verify(session, times(1)).invalidate();
    }

    @Test
    void adminLogout_sessionDoesNotExist_returnsOk() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminLogout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Déconnexion administrateur réussie.", response.getBody().get("message"));
        verify(session, never()).invalidate();
    }

    @Test
    void adminDashboard_adminLoggedIn_returnsOkAndWelcomeMessage() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("ADMIN_LOGGED_IN")).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bienvenue José.", response.getBody().get("message"));
    }

    @Test
    void adminDashboard_adminNotLoggedIn_returnsUnauthorized() {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("ADMIN_LOGGED_IN")).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Non autorisé.", response.getBody().get("message"));
    }

    @Test
    void adminDashboard_sessionDoesNotExist_returnsUnauthorized() {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.adminDashboard(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Non autorisé.", response.getBody().get("message"));
    }
}
