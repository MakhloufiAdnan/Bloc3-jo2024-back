package fr.studi.bloc3jo2024.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminSessionFilterTest {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionFilterTest.class);
    private static final String ADMIN_PATH_PREFIX = "/api/admin";
    private static final String ADMIN_AUTH_PATH_PREFIX = "/api/admin/auth";
    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";

    private static final String EXPECTED_ERROR_MESSAGE_CONTENT = "Accès refusé. Une session administrateur est requise.";
    private static final String EXPECTED_ERROR_TYPE = "Unauthorized";

    private AdminSessionFilter adminSessionFilter;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpSession session;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    // Méthode utilitaire pour la réflexion, maintenant elle lève RuntimeException en cas d'échec
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            String errorMessage = String.format("Échec de la configuration du test : Impossible de définir le champ privé '%s' sur %s",
                    fieldName, target.getClass().getSimpleName());
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @BeforeEach
    void setUp() throws IOException { // IOException pour response.getWriter()
        objectMapper = new ObjectMapper();
        adminSessionFilter = new AdminSessionFilter(objectMapper);

        // Configuration des champs privés via réflexion.
        // Si cela échoue, une RuntimeException sera levée par setPrivateField.
        setPrivateField(adminSessionFilter, "adminPathPrefix", ADMIN_PATH_PREFIX);
        setPrivateField(adminSessionFilter, "adminAuthPathPrefix", ADMIN_AUTH_PATH_PREFIX);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        // Simule response.getWriter() pour qu'il retourne notre PrintWriter qui écrit dans un StringWriter.
        // Cela peut lever une IOException, d'où la déclaration dans la signature de setUp.
        when(response.getWriter()).thenReturn(printWriter);

        // Définit explicitement que filterChain.doFilter ne fait rien et ne lève pas d'exception par défaut.
        // Les exceptions déclarées par doFilter (IOException, ServletException) sont toujours "possibles"
        // si la logique interne du filtre les propageait depuis cette invocation.
        try {
            doNothing().when(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        } catch (ServletException | IOException e) {
            // Ce catch est plus théorique ici car doNothing() sur un mock ne devrait pas lever ces exceptions.
            // Mais si la signature de la méthode doFilter changeait, cela pourrait devenir pertinent.
            throw new RuntimeException("Échec du stubbing de filterChain.doFilter", e);
        }
    }


    // La méthode d'assertion déclare maintenant aussi ServletException pour être symétrique
    // avec ce que doFilterInternal peut lever, même si les opérations internes
    // sont plus susceptibles de lever IOException.
    private void assertUnauthorizedResponse(String requestUri) throws IOException, ServletException {
        printWriter.flush();
        String jsonResponse = stringWriter.toString();

        assertNotNull(jsonResponse, "La réponse JSON ne devrait pas être nulle.");
        if (jsonResponse.trim().isEmpty()) {
            throw new AssertionError("La réponse JSON est vide, attendait un corps d'erreur. URI: " + requestUri);
        }

        Map<String, Object> responseMap;
        try {
            responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (IOException e) {
            throw new AssertionError("Échec de la désérialisation de la réponse JSON : '" + jsonResponse + "'", e);
        }

        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseMap.get("status"), "Le statut HTTP de la réponse d'erreur est incorrect.");
        assertEquals(EXPECTED_ERROR_TYPE, responseMap.get("error"), "Le type d'erreur dans la réponse JSON est incorrect.");
        assertEquals(EXPECTED_ERROR_MESSAGE_CONTENT, responseMap.get("message"), "Le message d'erreur dans la réponse JSON est incorrect.");
        assertEquals(requestUri, responseMap.get("path"), "Le chemin dans la réponse JSON d'erreur est incorrect.");
        assertNotNull(responseMap.get("timestamp"), "Le timestamp est manquant dans la réponse JSON d'erreur.");

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers des URI publiques")
    void doFilterInternal_publicUri_shouldProceed() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/public/resource");
        adminSessionFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers les URI d'authentification admin")
    void doFilterInternal_adminAuthUri_shouldProceed() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn(ADMIN_AUTH_PATH_PREFIX + "/login");
        adminSessionFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    @DisplayName("Doit refuser l'accès (401) pour une URI admin protégée sans session")
    void doFilterInternal_protectedAdminUri_noSession_shouldReturnUnauthorized() throws ServletException, IOException {
        String requestUri = ADMIN_PATH_PREFIX + "/dashboard";
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getSession(false)).thenReturn(null);

        adminSessionFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response, times(1)).getWriter();
        assertUnauthorizedResponse(requestUri);
    }

    @Test
    @DisplayName("Doit refuser l'accès (401) pour une URI admin protégée avec session mais sans attribut de connexion")
    void doFilterInternal_protectedAdminUri_sessionNoAttribute_shouldReturnUnauthorized() throws ServletException, IOException {
        String requestUri = ADMIN_PATH_PREFIX + "/users/1";
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SESSION_ADMIN_LOGGED_IN)).thenReturn(null);

        adminSessionFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response, times(1)).getWriter();
        assertUnauthorizedResponse(requestUri);
    }

    @Test
    @DisplayName("Doit laisser passer les requêtes vers une URI admin protégée avec session valide et attribut de connexion")
    void doFilterInternal_protectedAdminUri_validSessionAndAttribute_shouldProceed() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn(ADMIN_PATH_PREFIX + "/settings");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(SESSION_ADMIN_LOGGED_IN)).thenReturn(true);

        adminSessionFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
    }

    @Test
    @DisplayName("Doit gérer correctement les préfixes d'URI configurés (test sur un cas protégé)")
    void doFilterInternal_customPrefixes_shouldWork() throws ServletException, IOException {
        String customAdminPrefix = "/management";
        String customAuthPrefix = "/management/login";
        String requestUri = customAdminPrefix + "/users";

        setPrivateField(adminSessionFilter, "adminPathPrefix", customAdminPrefix);
        setPrivateField(adminSessionFilter, "adminAuthPathPrefix", customAuthPrefix);

        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getSession(false)).thenReturn(null);

        adminSessionFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response, times(1)).getWriter();
        assertUnauthorizedResponse(requestUri);
    }
}