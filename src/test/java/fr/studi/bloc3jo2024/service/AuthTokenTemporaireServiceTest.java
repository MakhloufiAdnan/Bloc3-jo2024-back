package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.AuthTokenTemporaire;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.repository.AuthTokenTemporaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenTemporaireServiceTest {

    @Mock
    private AuthTokenTemporaireRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthTokenTemporaireService authTokenService;

    private Utilisateur utilisateur;

    @BeforeEach
    void arrange_setup() {
        utilisateur = new Utilisateur();
    }

    @Test
    void act_createToken_assert_shouldGenerateAndSaveHashedToken() {
        // Arrange
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofHours(1);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-token");
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);

        // Act
        String token = authTokenService.createToken(utilisateur, type, validity);

        // Assert
        assertNotNull(token);
        verify(tokenRepository).save(tokenCaptor.capture());
        AuthTokenTemporaire savedToken = tokenCaptor.getValue();
        assertEquals("hashed-token", savedToken.getTokenHache());
        assertEquals(type, savedToken.getTypeToken());
        assertEquals(utilisateur, savedToken.getUtilisateur());
    }

    @Test
    void act_validateToken_assert_shouldReturnValidToken() {
        // Arrange
        String rawToken = "raw";
        String hashed = "hashed";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        AuthTokenTemporaire validToken = AuthTokenTemporaire.builder()
                .tokenHache(hashed)
                .typeToken(type)
                .utilisateur(utilisateur)
                .dateExpiration(LocalDateTime.now().plusMinutes(10))
                .build();

        validToken.setUsed(false);

        when(tokenRepository.findAll()).thenReturn(List.of(validToken));
        when(passwordEncoder.matches(rawToken, hashed)).thenReturn(true);

        // Act
        AuthTokenTemporaire result = authTokenService.validateToken(rawToken, type);

        // Assert
        assertNotNull(result);
        assertEquals(validToken, result);
    }

    @Test
    void act_validateToken_assert_shouldThrowExceptionIfTokenInvalid() {
        // Arrange
        String rawToken = "invalid";
        when(tokenRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD));
    }

    @Test
    void act_validateToken_assert_shouldThrowExceptionIfWrongType() {
        // Arrange
        String rawToken = "raw";
        String hashed = "hashed";
        AuthTokenTemporaire wrongTypeToken = AuthTokenTemporaire.builder()
                .tokenHache(hashed)
                .typeToken(TypeAuthTokenTemp.VALIDATION_EMAIL)
                .dateExpiration(LocalDateTime.now().plusMinutes(10))
                .utilisateur(utilisateur)
                .build();

        wrongTypeToken.setUsed(false);

        when(tokenRepository.findAll()).thenReturn(List.of(wrongTypeToken));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD));
    }

    @Test
    void act_markAsUsed_assert_shouldUpdateTokenAndSave() {
        // Arrange
        AuthTokenTemporaire token = new AuthTokenTemporaire();
        token.setUsed(false);
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);

        // Act
        authTokenService.markAsUsed(token);

        // Assert
        assertTrue(token.isUsed());
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals(token, tokenCaptor.getValue());
    }

    @Test
    void act_purgeExpiredTokens_assert_shouldCallRepositoryAndReturnDeletedCount() {
        // Arrange
        when(tokenRepository.deleteByDateExpirationBefore(any(LocalDateTime.class))).thenReturn(3L);

        // Act
        long deleted = authTokenService.purgeExpiredTokens();

        // Assert
        assertEquals(3L, deleted);
        verify(tokenRepository).deleteByDateExpirationBefore(any(LocalDateTime.class));
    }
}