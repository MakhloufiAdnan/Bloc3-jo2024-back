package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.controller.AdminController;
import fr.studi.bloc3jo2024.dto.authentification.LoginAdminRequestDto;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerMvcTest {

    private MockMvc mockMvc;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper objectMapperMock; // Nom différent pour éviter confusion si un autre ObjectMapper est utilisé localement

    @InjectMocks
    private AdminController adminController;

    private final String adminEmail = "admin-local@example.com";
    private final String adminPasswordPlainText = "AdminLocalPass123!";
    private final String adminPasswordHash = "hash_simule_pour_AdminLocalPass123!";
    private final ObjectMapper objectMapperForRequests = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // L'extension Mockito gère l'initialisation de adminController avec ses mocks.
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        // Configuration des champs @Value du contrôleur via ReflectionTestUtils
        ReflectionTestUtils.setField(adminController, "configuredAdminEmail", adminEmail);
        ReflectionTestUtils.setField(adminController, "configuredAdminPasswordHash", adminPasswordHash);
    }

    @Test
    void adminLogin_shouldReturnOkAndTokenWhenCredentialsAreValid() throws Exception {
        LoginAdminRequestDto validCredentials = new LoginAdminRequestDto();
        validCredentials.setEmail(adminEmail);
        validCredentials.setPassword(adminPasswordPlainText);

        // Simuler le comportement du passwordEncoder pour des identifiants valides
        when(passwordEncoder.matches(adminPasswordPlainText, adminPasswordHash)).thenReturn(true);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapperForRequests.writeValueAsString(validCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Connexion administrateur réussie."))
                .andExpect(jsonPath("$.token").value("admin-session-active"));
    }

    @Test
    void adminLogin_shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginAdminRequestDto invalidCredentials = new LoginAdminRequestDto();
        invalidCredentials.setEmail("wrong@example.com");
        invalidCredentials.setPassword("wrongpassword");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapperForRequests.writeValueAsString(invalidCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants administrateur invalides."));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedTrueWhenSessionAttributeExists() throws Exception {
        MockHttpSession session = new MockHttpSession();
        // Utiliser la constante définie dans AdminController pour la clé de session
        session.setAttribute(AdminController.SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(get("/api/admin/auth/check-session")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedFalseWhenSessionAttributeDoesNotExist() throws Exception {
        MockHttpSession session = new MockHttpSession(); // Session vide

        mockMvc.perform(get("/api/admin/auth/check-session")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedFalseWhenNoSessionProvided() throws Exception {
        // Aucun .session(session) n'est fourni, simulant une requête sans session existante
        mockMvc.perform(get("/api/admin/auth/check-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void adminLogout_shouldInvalidateSessionWhenSessionExists() throws Exception {
        TrackableMockHttpSession session = new TrackableMockHttpSession();
        session.setAttribute(AdminController.SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(post("/api/admin/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déconnexion administrateur réussie. Session invalidée."));

        assertTrue(session.isInvalidated(), "La session aurait dû être invalidée.");
    }

    @Test
    void adminLogout_shouldReturnOkWithMessageWhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/admin/auth/logout")) // Pas de session fournie
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Aucune session administrateur active à déconnecter."));
    }

    @Test
    void adminDashboard_shouldReturnOkWhenAdminIsLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AdminController.SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(get("/api/admin/auth/dashboard")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bienvenue sur le dashboard administrateur."));
    }

    @Test
    void adminDashboard_shouldReturnUnauthorizedWhenAdminIsNotLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession(); // Session existe mais admin non loggué

        mockMvc.perform(get("/api/admin/auth/dashboard")
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Non autorisé. Veuillez vous connecter."));
    }

    @Test
    void adminDashboard_shouldReturnUnauthorizedWhenNoSessionProvided() throws Exception {
        mockMvc.perform(get("/api/admin/auth/dashboard")) // Pas de session
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Non autorisé. Veuillez vous connecter."));
    }

    /**
     * Sous-classe de MockHttpSession pour suivre si la session a été invalidée.
     * Utile pour tester la déconnexion.
     */
    @Getter
    static class TrackableMockHttpSession extends MockHttpSession {
        private boolean invalidated = false;

        @Override
        public void invalidate() {
            super.invalidate();
            this.invalidated = true;
        }
    }
}