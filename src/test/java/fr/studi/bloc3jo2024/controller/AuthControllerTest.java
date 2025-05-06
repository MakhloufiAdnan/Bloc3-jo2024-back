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

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // Test de la méthode register avec le pattern AAA (Arrange, Act, Assert)
    @Test
    void register_validRequest_returnsCreatedAndSuccessMessage() {
        // Arrange : Préparer le test
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("MotDePasseUtilisateur123");
        request.setUsername("Studi");
        request.setFirstname("Bob");

        // Act : Exécuter l’action à tester
        ResponseEntity<AuthReponseDto> response = authController.register(request);

        // Assert : Vérifier le résultat
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Inscription réussie. Un email de confirmation a été envoyé.", response.getBody().getMessage());
        assertNull(response.getBody().getToken());
        verify(utilisateurService, times(1)).registerUser(request);
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
        String errorMessage = "Token invalide.";
        doThrow(new IllegalArgumentException(errorMessage)).when(utilisateurService).confirmUser(token);

        // Act
        ResponseEntity<String> response = authController.confirm(token);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Token invalide ou expiré. " + errorMessage, response.getBody());
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void confirm_accountAlreadyActivated_returnsConflictAndErrorMessage() {
        // Arrange
        String token = "usedToken";
        String errorMessage = "Compte déjà actif.";
        doThrow(new IllegalStateException(errorMessage)).when(utilisateurService).confirmUser(token);

        // Act
        ResponseEntity<String> response = authController.confirm(token);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Compte déjà activé ou lien déjà utilisé. " + errorMessage, response.getBody());
        verify(utilisateurService).confirmUser(token);
    }

    @Test
    void login_validCredentials_returnsOkAndAuthToken() {
        // Arrange
        LoginUtilisateurRequestDto request = new LoginUtilisateurRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        Authentication authResult = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
        when(authResult.getName()).thenReturn(request.getEmail());
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
        verify(jwtService, never()).generateToken(anyString());
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
        assertNull(response.getBody());
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
