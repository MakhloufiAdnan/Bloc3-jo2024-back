package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.controller.AdminController;
import fr.studi.bloc3jo2024.dto.authentification.LoginAdminRequestDto;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerMvcTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    private final String adminEmail = "admin@example.com";
    private final String adminPassword = "password";
    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        ReflectionTestUtils.setField(adminController, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(adminController, "adminPassword", adminPassword);
    }

    @Test
    void adminLogin_shouldReturnOkAndTokenWhenCredentialsAreValid() throws Exception {
        LoginAdminRequestDto validCredentials = new LoginAdminRequestDto();
        validCredentials.setEmail(adminEmail);
        validCredentials.setPassword(adminPassword);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Connexion administrateur réussie."))
                .andExpect(jsonPath("$.token").value("admin-session"));
    }

    @Test
    void adminLogin_shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginAdminRequestDto invalidCredentials = new LoginAdminRequestDto();
        invalidCredentials.setEmail("wrong@example.com");
        invalidCredentials.setPassword("wrongpassword");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants administrateur invalides."));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedTrueWhenSessionAttributeExists() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(get("/api/admin/auth/check-session")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedFalseWhenSessionAttributeDoesNotExist() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/api/admin/auth/check-session")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void checkAdminSession_shouldReturnAuthenticatedFalseWhenSessionIsNull() throws Exception {
        mockMvc.perform(get("/api/admin/auth/check-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void adminLogout_shouldInvalidateSessionWhenSessionExists() throws Exception {
        TrackableMockHttpSession session = new TrackableMockHttpSession();
        session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(post("/api/admin/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déconnexion administrateur réussie."));

        assertTrue(session.isInvalidated());
    }

    @Test
    void adminLogout_shouldReturnOkWhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Déconnexion administrateur réussie."));
    }

    @Test
    void adminDashboard_shouldReturnOkWhenAdminIsLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

        mockMvc.perform(get("/api/admin/auth/dashboard")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bienvenue José."));
    }

    @Test
    void adminDashboard_shouldReturnUnauthorizedWhenAdminIsNotLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/api/admin/auth/dashboard")
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Non autorisé."));
    }

    @Test
    void adminDashboard_shouldReturnUnauthorizedWhenSessionIsNull() throws Exception {
        mockMvc.perform(get("/api/admin/auth/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Non autorisé."));
    }

    // --- Sous-classe pour traquer l'invalidation de session ---
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
