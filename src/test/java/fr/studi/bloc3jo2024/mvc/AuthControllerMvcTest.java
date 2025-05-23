package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.studi.bloc3jo2024.controller.AuthController;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.exception.GlobalExceptionHandler;
import fr.studi.bloc3jo2024.service.JwtService;
import fr.studi.bloc3jo2024.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // Utilise l'extension Mockito pour JUnit 5
class AuthControllerMvcTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper; // Pour convertir les objets Java en JSON et vice-versa

    @BeforeEach
    void setUp() {
        // Initialise MockMvc pour tester le contrôleur en isolation
        // GlobalExceptionHandler est inclus pour tester la gestion des exceptions du contrôleur
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        // Configure ObjectMapper pour gérer les types Java Time (comme LocalDate)
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Méthode utilitaire pour créer un DTO d'inscription valide
    private RegisterRequestDto createValidRegisterRequestDto() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("testuserMvc");
        request.setFirstname("Jean");
        request.setDate(LocalDate.of(1990, 1, 1));
        request.setEmail("mvc_test@example.com");
        request.setPhonenumber("+33612345678");
        request.setStreetnumber(10);
        request.setAddress("10 rue de Paris"); // Note: Ce champ semble redondant si streetnumber est déjà là.
        request.setPostalcode("75000");
        request.setCity("Paris");
        request.setPassword("MotDePassMvc123!");
        request.setCountry("France");
        return request;
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        // Arrange
        RegisterRequestDto request = createValidRegisterRequestDto();
        // Simule le service utilisateur ne faisant rien (pas d'exception)
        doNothing().when(utilisateurService).registerUser(any(RegisterRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Convertit l'objet en JSON
                .andExpect(status().isCreated()) // Vérifie le statut HTTP 201
                .andExpect(jsonPath("$.message").value("Inscription réussie. Un email de confirmation a été envoyé.")); // Vérifie le message dans le corps JSON

        verify(utilisateurService).registerUser(any(RegisterRequestDto.class)); // Vérifie l'appel au service
    }

    @Test
    void confirm_shouldReturnOk_whenTokenIsValid() throws Exception {
        // Arrange
        String token = "validToken";
        doNothing().when(utilisateurService).confirmUser(token);

        // Act & Assert
        mockMvc.perform(get("/api/auth/confirm")
                        .param("token", token)) // Ajoute le token comme paramètre de requête
                .andExpect(status().isOk()) // Vérifie le statut HTTP 200
                .andExpect(content().string("Compte activé. Vous pouvez désormais vous connecter.")); // Vérifie le corps de la réponse texte

        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        // Arrange
        String token = "invalidToken";
        String serviceErrorMessage = "Token invalide pour ce test MVC"; // Message spécifique du mock pour le test
        // Simule le service utilisateur levant une IllegalArgumentException
        doThrow(new IllegalArgumentException(serviceErrorMessage)).when(utilisateurService).confirmUser(token);

        // Act & Assert
        mockMvc.perform(get("/api/auth/confirm")
                        .param("token", token))
                .andExpect(status().isBadRequest()) // Vérifie le statut HTTP 400
                // Le corps de la réponse doit correspondre à ce que AuthController construit
                .andExpect(content().string("Lien de confirmation invalide ou expiré. " + serviceErrorMessage));

        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_shouldReturnConflict_whenAccountAlreadyActivated() throws Exception {
        // Arrange
        String token = "usedToken";
        String serviceErrorMessage = "Compte déjà utilisé pour ce test MVC";
        doThrow(new IllegalStateException(serviceErrorMessage)).when(utilisateurService).confirmUser(token);

        // Act & Assert
        mockMvc.perform(get("/api/auth/confirm")
                        .param("token", token))
                .andExpect(status().isConflict()) // Vérifie le statut HTTP 409
                .andExpect(content().string("Ce compte est déjà activé ou le lien de confirmation a déjà été utilisé. " + serviceErrorMessage));
        verify(utilisateurService).confirmUser(token);
    }


    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        // Arrange
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("MotDePass123!");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(request.getEmail());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtService.generateToken(request.getEmail())).thenReturn("mockJwtToken");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Connexion réussie"))
                .andExpect(jsonPath("$.token").value("mockJwtToken"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(request.getEmail());
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        // Simule l'AuthenticationManager levant une BadCredentialsException
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Email ou mot de passe invalide.")); // Le message doit correspondre à celui attendu

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou mot de passe invalide.")); // Vérifie le message d'erreur

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString()); // S'assure que le token n'est pas généré
    }

    @Test
    void passwordResetRequest_shouldReturnNoContent_whenEmailIsValid() throws Exception {
        // Arrange
        String email = "test@example.com";
        doNothing().when(utilisateurService).requestPasswordReset(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/password-reset-request")
                        .param("email", email)) // Envoi comme paramètre de requête
                .andExpect(status().isNoContent()); // Vérifie le statut HTTP 204

        verify(utilisateurService).requestPasswordReset(email);
    }

    @Test
    void passwordReset_shouldReturnNoContent_whenResetIsSuccessful() throws Exception {
        // Arrange
        String token = "validToken";
        String newPassword = "NewPassword123!";
        doNothing().when(utilisateurService).resetPassword(token, newPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/password-reset")
                        .param("token", token)
                        .param("newPassword", newPassword))
                .andExpect(status().isNoContent());

        verify(utilisateurService).resetPassword(token, newPassword);
    }

    @Test
    void passwordReset_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        // Arrange
        String token = "invalidToken";
        String newPassword = "NewPassword123!";
        // Simule le service levant une IllegalArgumentException pour un token invalide
        doThrow(new IllegalArgumentException("Token de réinitialisation invalide ou expiré."))
                .when(utilisateurService).resetPassword(token, newPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/password-reset")
                        .param("token", token)
                        .param("newPassword", newPassword))
                .andExpect(status().isBadRequest()); // Attend un statut 400 Bad Request

        verify(utilisateurService).resetPassword(token, newPassword);
    }
}