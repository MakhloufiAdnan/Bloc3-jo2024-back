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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe {@link AuthTokenTemporaireService}.
 * Utilise Mockito pour simuler les dépendances (repository, passwordEncoder).
 */
@ExtendWith(MockitoExtension.class)
class AuthTokenTemporaireServiceTest {

    @Mock
    private AuthTokenTemporaireRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthTokenTemporaireService authTokenService;

    private Utilisateur utilisateurTest;
    private String defaultTokenIdentifier;

    @BeforeEach
    void setUp() {
        utilisateurTest = new Utilisateur();
        utilisateurTest.setIdUtilisateur(UUID.randomUUID());
        utilisateurTest.setEmail("testuser@example.com");
        defaultTokenIdentifier = UUID.randomUUID().toString();
    }

    /**
     * Teste la création d'un token : vérifie la génération, le hachage et la sauvegarde.
     */
    @Test
    void act_createToken_assert_shouldGenerateAndSaveHashedToken() {
        // Arrange
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;
        Duration validity = Duration.ofHours(1);
        String expectedHashedToken = "hashed-token-example";

        when(passwordEncoder.encode(anyString())).thenReturn(expectedHashedToken);
        when(tokenRepository.findByUtilisateurAndTypeToken(utilisateurTest, type)).thenReturn(Optional.empty());
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);

        // Act
        String rawTokenReturned = authTokenService.createToken(utilisateurTest, type, validity);

        // Assert
        assertNotNull(rawTokenReturned, "Le token brut généré ne devrait pas être null.");
        assertFalse(rawTokenReturned.isEmpty(), "Le token brut généré ne devrait pas être vide.");

        verify(tokenRepository).save(tokenCaptor.capture());
        AuthTokenTemporaire savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getTokenIdentifier(), "Le tokenIdentifier sauvegardé ne devrait pas être null.");
        assertEquals(rawTokenReturned, savedToken.getTokenIdentifier(), "Le tokenIdentifier sauvegardé devrait correspondre au rawToken retourné.");
        assertEquals(expectedHashedToken, savedToken.getTokenHache(), "Le token haché sauvegardé ne correspond pas.");
        assertEquals(type, savedToken.getTypeToken(), "Le type du token sauvegardé ne correspond pas.");
        assertEquals(utilisateurTest, savedToken.getUtilisateur(), "L'utilisateur associé au token ne correspond pas.");
        assertFalse(savedToken.isUsed(), "Le token devrait être initialement marqué comme non utilisé.");
        assertTrue(savedToken.getDateExpiration().isAfter(LocalDateTime.now().minusSeconds(5)),
                "La date d'expiration du token devrait être dans le futur.");
    }

    /**
     * Teste la validation d'un token valide.
     */
    @Test
    void act_validateToken_assert_shouldReturnValidToken_whenTokenIsValid() {
        // Arrange
        String rawTokenIdentifier = defaultTokenIdentifier; // Utilise l'UUID généré dans setUp
        String correspondingHashedSecret = "corresponding-hash-for-" + rawTokenIdentifier;
        TypeAuthTokenTemp expectedType = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire tokenFromDb = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(correspondingHashedSecret) // Doit correspondre pour passwordEncoder.matches
                .typeToken(expectedType)
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();

        // Simuler que le repository trouve ce token via son identifiant
        when(tokenRepository.findByTokenIdentifier(rawTokenIdentifier)).thenReturn(Optional.of(tokenFromDb));
        // Simuler que passwordEncoder.matches retourne true pour ce rawToken et ce hash
        when(passwordEncoder.matches(rawTokenIdentifier, correspondingHashedSecret)).thenReturn(true);

        // Act
        AuthTokenTemporaire result = authTokenService.validateToken(rawTokenIdentifier, expectedType);

        // Assert
        assertNotNull(result);
        assertEquals(tokenFromDb.getIdTokenTemp(), result.getIdTokenTemp());
        assertEquals(rawTokenIdentifier, result.getTokenIdentifier());
    }

    /**
     * Teste la validation d'un token lorsque celui-ci n'est pas trouvé par son identifiant.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenNotFound() {
        // Arrange
        String rawTokenInexistant = "ce-token-n-existe-pas-en-base";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        // Simuler que findByTokenIdentifier ne retourne rien
        when(tokenRepository.findByTokenIdentifier(rawTokenInexistant)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawTokenInexistant, type)
        );
        assertEquals("Token non trouvé.", exception.getMessage());
    }

    /**
     * Teste la validation d'un token dont le secret (hachage) ne correspond pas.
     * Cela corrige le problème où les tests précédents retournaient "Token non trouvé"
     * car ils n'atteignaient pas les vérifications ultérieures.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenSecretMismatch() {
        // Arrange
        String rawTokenIdentifier = defaultTokenIdentifier;
        String storedHashedSecret = "un-certain-hash-stocke";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire tokenFromDb = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenIdentifier(rawTokenIdentifier) // Ce token sera trouvé
                .tokenHache(storedHashedSecret)      // Mais son hash ne correspondra pas à rawTokenIdentifier
                .typeToken(type)
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(tokenRepository.findByTokenIdentifier(rawTokenIdentifier)).thenReturn(Optional.of(tokenFromDb));
        // Simuler l'échec de la vérification du secret
        when(passwordEncoder.matches(rawTokenIdentifier, storedHashedSecret)).thenReturn(false);


        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawTokenIdentifier, type)
        );
        assertEquals("Incohérence de sécurité du token. Vérification du secret échouée.", exception.getMessage());
    }


    /**
     * Teste la validation d'un token qui a déjà été utilisé.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenIsUsed() {
        // Arrange
        String rawTokenIdentifier = defaultTokenIdentifier;
        String correspondingHashedSecret = "hash-du-token-utilise";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire usedToken = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(correspondingHashedSecret)
                .typeToken(type)
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(true) // Marqué comme utilisé
                .build();

        when(tokenRepository.findByTokenIdentifier(rawTokenIdentifier)).thenReturn(Optional.of(usedToken));
        when(passwordEncoder.matches(rawTokenIdentifier, correspondingHashedSecret)).thenReturn(true); // Le secret correspond

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawTokenIdentifier, type)
        );
        assertEquals("Token déjà utilisé.", exception.getMessage());
    }

    /**
     * Teste la validation d'un token qui a expiré.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenIsExpired() {
        // Arrange
        String rawTokenIdentifier = defaultTokenIdentifier;
        String correspondingHashedSecret = "hash-du-token-expire";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(correspondingHashedSecret)
                .typeToken(type)
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().minusSeconds(1)) // Expiré
                .isUsed(false)
                .build();

        when(tokenRepository.findByTokenIdentifier(rawTokenIdentifier)).thenReturn(Optional.of(expiredToken));
        when(passwordEncoder.matches(rawTokenIdentifier, correspondingHashedSecret)).thenReturn(true); // Le secret correspond

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawTokenIdentifier, type)
        );
        assertEquals("Token expiré.", exception.getMessage());
    }

    /**
     * Teste la validation d'un token dont le type ne correspond pas au type attendu.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalState_whenTokenTypeIsWrong() {
        // Arrange
        String rawTokenIdentifier = defaultTokenIdentifier;
        String correspondingHashedSecret = "hash-mauvais-type";
        TypeAuthTokenTemp typeDansLeToken = TypeAuthTokenTemp.VALIDATION_EMAIL;
        TypeAuthTokenTemp typeAttenduParTest = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire tokenAvecMauvaisType = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenIdentifier(rawTokenIdentifier)
                .tokenHache(correspondingHashedSecret)
                .typeToken(typeDansLeToken) // Type incorrect pour ce test
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(tokenRepository.findByTokenIdentifier(rawTokenIdentifier)).thenReturn(Optional.of(tokenAvecMauvaisType));
        when(passwordEncoder.matches(rawTokenIdentifier, correspondingHashedSecret)).thenReturn(true); // Le secret correspond

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                authTokenService.validateToken(rawTokenIdentifier, typeAttenduParTest)
        );
        String expectedMessage = "Type de token incorrect. Attendu: " + typeAttenduParTest + ", Obtenu: " + typeDansLeToken;
        assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Teste la méthode markAsUsed.
     */
    @Test
    void act_markAsUsed_assert_shouldUpdateTokenAndSave() {
        // Arrange
        UUID tokenId = UUID.randomUUID();
        String tokenIdentifierPourCeTest = UUID.randomUUID().toString(); // Doit être initialisé
        String tokenHachePourCeTest = "un-hash-quelconque";         // Doit être initialisé

        AuthTokenTemporaire tokenExistantEnBaseSimule = AuthTokenTemporaire.builder()
                .idTokenTemp(tokenId)
                .tokenIdentifier(tokenIdentifierPourCeTest) // Initialiser ce champ
                .tokenHache(tokenHachePourCeTest)           // Initialiser ce champ
                .utilisateur(utilisateurTest)
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenExistantEnBaseSimule));
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);

        AuthTokenTemporaire tokenParametrePourService = new AuthTokenTemporaire();
        tokenParametrePourService.setIdTokenTemp(tokenId);

        // Act
        authTokenService.markAsUsed(tokenParametrePourService);

        // Assert
        verify(tokenRepository).save(tokenCaptor.capture());
        AuthTokenTemporaire tokenSauvegarde = tokenCaptor.getValue();

        assertTrue(tokenSauvegarde.isUsed(), "Le token sauvegardé devrait être marqué comme 'utilisé'.");
        assertEquals(tokenId, tokenSauvegarde.getIdTokenTemp());
        // Vérifier que c'est bien l'instance mockée qui a été modifiée
        assertSame(tokenExistantEnBaseSimule, tokenSauvegarde, "L'instance sauvegardée devrait être celle retournée par findById.");
    }

    /**
     * Teste la purge des tokens expirés.
     */
    @Test
    void act_purgeExpiredTokens_assert_shouldCallRepositoryAndReturnDeletedCount() {
        // Arrange
        long expectedDeletedCount = 3L;
        when(tokenRepository.deleteByDateExpirationBefore(any(LocalDateTime.class))).thenReturn(expectedDeletedCount);

        // Act
        long actualDeletedCount = authTokenService.purgeExpiredTokens();

        // Assert
        assertEquals(expectedDeletedCount, actualDeletedCount);
        verify(tokenRepository).deleteByDateExpirationBefore(any(LocalDateTime.class));
    }
}