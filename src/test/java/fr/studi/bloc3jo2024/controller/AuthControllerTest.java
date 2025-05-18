package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.service.JwtService;
import fr.studi.bloc3jo2024.service.UtilisateurService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    private AutoCloseable closeable; // Pour gérer la fermeture des mocks

    @BeforeEach
    void setUp() {
        // Initialise les mocks et l'objet sous test
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Ferme les ressources de mocking après chaque test
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void register_validRequest_returnsCreatedAndSuccessMessage() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("MotDePasseUtilisateur123");
        request.setUsername("Studi");
        request.setFirstname("Bob");
        // Simule le comportement du service utilisateur (ne rien faire et ne pas lever d'exception)
        doNothing().when(utilisateurService).registerUser(any(RegisterRequestDto.class));

        // Act
        ResponseEntity<AuthReponseDto> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Inscription réussie. Un email de confirmation a été envoyé.", response.getBody().getMessage());
        assertNull(response.getBody().getToken(), "Aucun token ne doit être retourné lors de l'inscription.");
        verify(utilisateurService, times(1)).registerUser(request); // Vérifie que la méthode du service a été appelée
    }

    @Test
    void confirm_validToken_returnsOkAndAccountActivatedMessage() {
        // Arrange
        String token = "validToken";
        doNothing().when(utilisateurService).confirmUser(token);

        // Act
        ResponseEntity<String> response = authController.confirm(token);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Compte activé. Vous pouvez désormais vous connecter.", response.getBody());
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_invalidToken_returnsBadRequestAndErrorMessage() {
        // Arrange
        String token = "invalidToken";
        String serviceErrorMessage = "Token invalide."; // Le message que le service est censé retourner via l'exception
        // Simule le service utilisateur levant une IllegalArgumentException
        doThrow(new IllegalArgumentException(serviceErrorMessage)).when(utilisateurService).confirmUser(token);

        // Act
        ResponseEntity<String> response = authController.confirm(token);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Le message attendu doit correspondre à celui construit par AuthController
        assertEquals("Lien de confirmation invalide ou expiré. " + serviceErrorMessage, response.getBody());
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_accountAlreadyActivated_returnsConflictAndErrorMessage() {
        // Arrange
        String token = "usedToken";
        String serviceErrorMessage = "Compte déjà actif."; // Le message que le service est censé retourner via l'exception
        // Simule le service utilisateur levant une IllegalStateException
        doThrow(new IllegalStateException(serviceErrorMessage)).when(utilisateurService).confirmUser(token);

        // Act
        ResponseEntity<String> response = authController.confirm(token);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        // Le message attendu doit correspondre à celui construit par AuthController
        assertEquals("Ce compte est déjà activé ou le lien de confirmation a déjà été utilisé. " + serviceErrorMessage, response.getBody());
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void login_validCredentials_returnsOkAndAuthToken() {
        // Arrange
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        Authentication mockAuthentication = mock(Authentication.class); // Crée un mock pour l'objet Authentication
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn(request.getEmail()); // Simule getName() sur le mock
        when(jwtService.generateToken(request.getEmail())).thenReturn("mockedJwtToken");

        // Act
        ResponseEntity<AuthReponseDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Connexion réussie", response.getBody().getMessage());
        assertEquals("mockedJwtToken", response.getBody().getToken());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(request.getEmail());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorizedAndErrorMessage() {
        // Arrange
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("invalid@example.com");
        request.setPassword("wrongpassword");

        // Simule l'AuthenticationManager levant une BadCredentialsException
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<AuthReponseDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email ou mot de passe invalide.", response.getBody().getMessage());
        assertNull(response.getBody().getToken());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString()); // Vérifie que generateToken n'est jamais appelé
    }

    @Test
    void passwordResetRequest_validEmail_returnsNoContent() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(utilisateurService).requestPasswordReset(email);

        // Act
        ResponseEntity<Void> response = authController.passwordResetRequest(email);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody()); // Pas de corps pour 204 No Content
        verify(utilisateurService).requestPasswordReset(email);
    }

    @Test
    void passwordReset_validTokenAndNewPassword_returnsNoContent() {
        // Arrange
        String token = "resetToken";
        String newPassword = "newStrongPassword123";
        doNothing().when(utilisateurService).resetPassword(token, newPassword);

        // Act
        ResponseEntity<Void> response = authController.passwordReset(token, newPassword);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(utilisateurService).resetPassword(token, newPassword);
    }
}
