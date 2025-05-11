package fr.studi.bloc3jo2024.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminSessionFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionFilterTest.class);
    private static final String ADMIN_PATH_PREFIX = "/api/admin";
    private static final String ADMIN_AUTH_PATH_PREFIX = "/api/admin/auth";
    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";
    private static final String UNAUTHORIZED_MESSAGE = "{\"error\": \"Admin non connecté.\"}";

    private AdminSessionFilter adminSessionFilter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpSession session;

    private StringWriter stringWriter;
    private PrintWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        adminSessionFilter = new AdminSessionFilter();

        try {
            Field adminPrefixField = AdminSessionFilter.class.getDeclaredField("adminPathPrefix");
            adminPrefixField.setAccessible(true);
            adminPrefixField.set(adminSessionFilter, ADMIN_PATH_PREFIX);

            Field adminAuthPrefixField = AdminSessionFilter.class.getDeclaredField("adminAuthPathPrefix");
            adminAuthPrefixField.setAccessible(true);
            adminAuthPrefixField.set(adminSessionFilter, ADMIN_AUTH_PATH_PREFIX);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Erreur lors de la configuration des champs privés du filtre via réflexion.", e);
        }

        stringWriter = new StringWriter();
        responseWriter = new PrintWriter(stringWriter); // responseWriter est créé ici
        when(response.getWriter()).thenReturn(responseWriter); // response.getWriter() retourne l'objet mocké responseWriter
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers des URI publiques")
    void doFilterInternal_publicUri_shouldProceed() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/public/resource");

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setContentType(anyString());
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers les URI d'authentification admin")
    void doFilterInternal_adminAuthUri_shouldProceed() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/auth/login");

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setContentType(anyString());
    }

    @Test
    @DisplayName("Doit refuser l'accès (401) pour une URI admin protégée sans session")
    void doFilterInternal_protectedAdminUri_noSession_shouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/dashboard");
        when(request.getSession(false)).thenReturn(null);

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
        responseWriter.flush();
        assertEquals(UNAUTHORIZED_MESSAGE, stringWriter.toString());

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Doit refuser l'accès (401) pour une URI admin protégée avec session mais sans attribut de connexion")
    void doFilterInternal_protectedAdminUri_sessionNoAttribute_shouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/users/1");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SESSION_ADMIN_LOGGED_IN)).thenReturn(null);

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");

        responseWriter.flush();
        assertEquals(UNAUTHORIZED_MESSAGE, stringWriter.toString());

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers une URI admin protégée avec session valide et attribut de connexion")
    void doFilterInternal_protectedAdminUri_validSessionAndAttribute_shouldProceed() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/settings");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SESSION_ADMIN_LOGGED_IN)).thenReturn(true);

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setContentType(anyString());
    }

    @Test
    @DisplayName("Doit gérer correctement les préfixes d'URI configurés (test sur un cas protégé)")
    void doFilterInternal_customPrefixes_shouldWork() throws ServletException, IOException {
        // Arrange
        String customAdminPrefix = "/management";
        String customAuthPrefix = "/management/login";

        try {
            Field adminPrefixField = AdminSessionFilter.class.getDeclaredField("adminPathPrefix");
            adminPrefixField.setAccessible(true);
            adminPrefixField.set(adminSessionFilter, customAdminPrefix);

            Field adminAuthPrefixField = AdminSessionFilter.class.getDeclaredField("adminAuthPathPrefix");
            adminAuthPrefixField.setAccessible(true);
            adminAuthPrefixField.set(adminSessionFilter, customAuthPrefix);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Erreur lors de la configuration des champs privés personnalisés.", e);
        }

        when(request.getRequestURI()).thenReturn("/management/users");
        when(request.getSession(false)).thenReturn(null);

        // Act
        adminSessionFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
        responseWriter.flush();
        assertEquals(UNAUTHORIZED_MESSAGE, stringWriter.toString());
        verify(filterChain, never()).doFilter(request, response);
    }
}