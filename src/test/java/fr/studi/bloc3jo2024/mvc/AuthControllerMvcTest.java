package fr.studi.bloc3jo2024.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.studi.bloc3jo2024.controller.AuthController;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
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

@ExtendWith(MockitoExtension.class)
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configurer MockMvc pour inclure le AuthenticationManager mocké dans le contrôleur
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Ajouté pour gérer LocalDate
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        // Création d'un objet RegisterRequestDto
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("testuser");
        request.setFirstname("Jean");
        request.setDate(LocalDate.of(1990, 1, 1));
        request.setEmail("test@example.com");
        request.setStreetnumber(10);
        request.setAddress("10 rue de Paris");
        request.setPostalcode("75000");
        request.setCity("Paris");
        request.setPassword("MotDePass123!");
        request.setCountry("France");

        // Simule le comportement de la méthode registerUser
        doNothing().when(utilisateurService).registerUser(request);

        // Effectuer l'appel POST et vérifier le statut
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Vérifie si le statut est 201 Created
                .andExpect(jsonPath("$.message").value("Inscription réussie. Un email de confirmation a été envoyé.")); // Vérifie le message

        // Vérifie que la méthode registerUser a bien été appelée
        verify(utilisateurService).registerUser(request);
    }

    @Test
    void confirm_shouldReturnOk_whenTokenIsValid() throws Exception {
        String token = "validToken";

        // Simuler la confirmation du token
        doNothing().when(utilisateurService).confirmUser(token);

        // Effectuer l'appel GET pour confirmer le token
        mockMvc.perform(get("/auth/confirm")
                        .param("token", token))
                .andExpect(status().isOk()) // Vérifie que la réponse est OK
                .andExpect(content().string("Compte activé. Vous pouvez désormais vous connecter.")); // Vérifie le message

        // Vérifie que la méthode confirmUser a bien été appelée
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        String token = "invalidToken";

        // Simuler une exception pour un token invalide
        doThrow(new IllegalArgumentException("Token invalide")).when(utilisateurService).confirmUser(token);

        // Effectuer l'appel GET pour confirmer le token
        mockMvc.perform(get("/auth/confirm")
                        .param("token", token))
                .andExpect(status().isBadRequest()) // Vérifie que la réponse est BAD_REQUEST (400)
                .andExpect(content().string("Token invalide ou expiré. Token invalide")); // Vérifie le message d'erreur

        // Vérifie que la méthode confirmUser a bien été appelée
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() throws Exception {
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("MotDePass123!");

        // Simuler l'authentification réussie via AuthenticationManager
        // Créer un objet Authentication mocké pour simuler le résultat d'une authentification réussie
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(request.getEmail()); // Simule l'obtention de l'email de l'utilisateur authentifié

        // Mock l'appel à authenticationManager.authenticate pour retourner l'objet Authentication mocké
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication); // Mock l'authentification réussie

        // Simuler la génération du JWT APRES l'authentification réussie
        when(jwtService.generateToken(request.getEmail())).thenReturn("mockJwtToken"); // MMock avec l'email attendu

        // Effectuer l'appel POST pour la connexion
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Vérifie que le statut est 200 OK
                .andExpect(jsonPath("$.message").value("Connexion réussie")) // Vérifie le message
                .andExpect(jsonPath("$.token").value("mockJwtToken"));  // Vérifie le JWT généré

        // Vérifier que authenticationManager.authenticate et jwtService.generateToken ont été appelés
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class)); // Vérification ajoutée
        verify(jwtService).generateToken(request.getEmail()); // Vérification
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        // Simuler un échec d'authentification via AuthenticationManager
        // Mock l'appel à authenticationManager.authenticate pour lancer une exception
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Email ou mot de passe invalide")); // Ajouté : Mock l'échec d'authentification

        // Effectuer l'appel POST pour la connexion
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // Vérifie que le statut est 401 Unauthorized
                // Vérifie que le contrôleur renvoie le message d'échec attendu
                .andExpect(jsonPath("$.message").value("Email ou mot de passe invalide.")); // Vérifie le message d'erreur

        // Vérifier que authenticationManager.authenticate a été appelé
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class)); // Vérification ajoutée
        // Vérifier que jwtService.generateToken n'a PAS été appelé en cas d'échec d'authentification
        verify(jwtService, never()).generateToken(anyString()); // Vérification ajoutée
    }

    @Test
    void passwordResetRequest_shouldReturnNoContent_whenEmailIsValid() throws Exception {
        String email = "test@example.com";

        // Simuler la demande de réinitialisation de mot de passe
        doNothing().when(utilisateurService).requestPasswordReset(email);

        // Effectuer l'appel POST pour demander une réinitialisation
        mockMvc.perform(post("/auth/password-reset-request")
                        .param("email", email))
                .andExpect(status().isNoContent()); // Vérifie que la réponse est No Content (204)

        // Vérifie que la méthode requestPasswordReset a bien été appelée
        verify(utilisateurService).requestPasswordReset(email);
    }

    @Test
    void passwordReset_shouldReturnNoContent_whenResetIsSuccessful() throws Exception {
        String token = "validToken";
        String newPassword = "NewPassword123!";

        // Simuler la réinitialisation du mot de passe
        doNothing().when(utilisateurService).resetPassword(token, newPassword);

        // Effectuer l'appel POST pour réinitialiser le mot de passe
        mockMvc.perform(post("/auth/password-reset")
                        .param("token", token)
                        .param("newPassword", newPassword))
                .andExpect(status().isNoContent()); // Vérifie que la réponse est No Content (204)

        // Vérifie que la méthode resetPassword a bien été appelée
        verify(utilisateurService).resetPassword(token, newPassword);
    }
}