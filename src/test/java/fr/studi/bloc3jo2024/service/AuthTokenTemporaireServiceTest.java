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
@ExtendWith(MockitoExtension.class) // Active l'extension Mockito pour JUnit 5
class AuthTokenTemporaireServiceTest {

    @Mock // Crée un mock pour AuthTokenTemporaireRepository
    private AuthTokenTemporaireRepository tokenRepository;

    @Mock // Crée un mock pour PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Crée une instance de AuthTokenTemporaireService et y injecte les mocks ci-dessus
    private AuthTokenTemporaireService authTokenService;

    private Utilisateur utilisateurTest; // Utilisateur de test commun, initialisé dans setUp

    /**
     * Configuration initiale avant chaque test.
     * Initialise un objet Utilisateur commun pour les tests.
     */
    @BeforeEach
    void setUp() {
        utilisateurTest = new Utilisateur();
        utilisateurTest.setIdUtilisateur(UUID.randomUUID()); // Important pour les logs et certaines logiques
        utilisateurTest.setEmail("testuser@example.com");   // Important pour les logs
        // Configurez d'autres champs de 'utilisateurTest' si nécessaire pour des tests spécifiques.
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

        // Simuler l'encodeur de mot de passe
        // anyString() est utilisé car le rawToken est généré avec UUID.randomUUID().toString()
        when(passwordEncoder.encode(anyString())).thenReturn(expectedHashedToken);

        // Simuler que findByUtilisateurAndTypeToken ne trouve aucun token existant (cas nominal)
        when(tokenRepository.findByUtilisateurAndTypeToken(utilisateurTest, type)).thenReturn(Optional.empty());

        // Capturer l'argument passé à tokenRepository.save()
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);

        // Act : Appel de la méthode à tester
        String rawToken = authTokenService.createToken(utilisateurTest, type, validity);

        // Assert : Vérifications
        assertNotNull(rawToken, "Le token brut généré ne devrait pas être null.");
        assertFalse(rawToken.isEmpty(), "Le token brut généré ne devrait pas être vide.");

        // Vérifier que la méthode save du repository a été appelée une fois
        verify(tokenRepository).save(tokenCaptor.capture());
        AuthTokenTemporaire savedToken = tokenCaptor.getValue(); // Obtenir le token qui aurait été sauvegardé

        assertEquals(expectedHashedToken, savedToken.getTokenHache(), "Le token haché sauvegardé ne correspond pas.");
        assertEquals(type, savedToken.getTypeToken(), "Le type du token sauvegardé ne correspond pas.");
        assertEquals(utilisateurTest, savedToken.getUtilisateur(), "L'utilisateur associé au token ne correspond pas.");
        assertFalse(savedToken.isUsed(), "Le token devrait être initialement marqué comme non utilisé.");
        assertTrue(savedToken.getDateExpiration().isAfter(LocalDateTime.now().minusSeconds(5)), // Permettre une petite marge pour l'exécution
                "La date d'expiration du token devrait être dans le futur.");
    }

    /**
     * Teste la validation d'un token valide.
     * S'assure que le service retourne le token attendu sans erreur.
     */
    @Test
    void act_validateToken_assert_shouldReturnValidToken_whenTokenIsValid() {
        // Arrange
        String rawToken = "raw-token-valide-pour-test";
        String hashedTokenInDb = "hash-correspondant-au-raw-token-valide";
        TypeAuthTokenTemp expectedType = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire tokenFromDb = AuthTokenTemporaire.builder()
                .idTokenTemp(UUID.randomUUID())
                .tokenHache(hashedTokenInDb)
                .typeToken(expectedType)
                .utilisateur(utilisateurTest) // Utilisateur initialisé dans setUp
                .dateExpiration(LocalDateTime.now().plusMinutes(30)) // Non expiré
                .isUsed(false) // Non utilisé
                .build();

        // Simuler que le repository trouve ce token parmi tous les tokens (selon la logique de service raffinée)
        when(tokenRepository.findAll()).thenReturn(List.of(tokenFromDb));
        // Simuler que le passwordEncoder confirme que le rawToken correspond au hash du token en base
        when(passwordEncoder.matches(rawToken, hashedTokenInDb)).thenReturn(true);

        // Act
        AuthTokenTemporaire result = authTokenService.validateToken(rawToken, expectedType);

        // Assert
        assertNotNull(result, "Un token valide devrait être retourné par le service.");
        assertEquals(tokenFromDb.getIdTokenTemp(), result.getIdTokenTemp(), "L'ID du token retourné ne correspond pas.");
        assertEquals(hashedTokenInDb, result.getTokenHache(), "Le hash du token retourné ne correspond pas.");
        assertEquals(expectedType, result.getTypeToken(), "Le type du token retourné ne correspond pas.");
    }

    /**
     * Teste la validation d'un token lorsque celui-ci n'est pas trouvé.
     * S'attend à une IllegalArgumentException avec un message spécifique.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenNotFound() {
        // Arrange
        String rawTokenInexistant = "ce-token-n-existe-pas-en-base";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        // Simuler que findAll ne retourne aucun token, donc aucun ne peut correspondre au rawToken
        when(tokenRepository.findAll()).thenReturn(Collections.emptyList());
        // passwordEncoder.matches ne sera pas appelé si aucun token n'est trouvé.

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawTokenInexistant, type)
        );
        assertEquals("Token non trouvé.", exception.getMessage(), "Le message d'erreur pour token non trouvé est incorrect.");
    }

    /**
     * Teste la validation d'un token qui a déjà été utilisé.
     * S'attend à une IllegalArgumentException avec un message spécifique.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenIsUsed() {
        // Arrange
        String rawToken = "token-deja-utilise-dans-test";
        String hashedToken = "hash-du-token-utilise";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire usedToken = AuthTokenTemporaire.builder()
                .tokenHache(hashedToken).typeToken(type).utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusHours(1)) // Non expiré
                .isUsed(true) // Marqué comme utilisé
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(usedToken));
        when(passwordEncoder.matches(rawToken, hashedToken)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawToken, type)
        );
        assertEquals("Token déjà utilisé.", exception.getMessage(), "Message d'erreur incorrect pour token déjà utilisé.");
    }

    /**
     * Teste la validation d'un token qui a expiré.
     * S'attend à une IllegalArgumentException avec un message spécifique.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalArgument_whenTokenIsExpired() {
        // Arrange
        String rawToken = "token-expire-pour-test";
        String hashedToken = "hash-du-token-expire";
        TypeAuthTokenTemp type = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire expiredToken = AuthTokenTemporaire.builder()
                .tokenHache(hashedToken).typeToken(type).utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().minusSeconds(1)) // Expiré
                .isUsed(false) // Non utilisé
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(expiredToken));
        when(passwordEncoder.matches(rawToken, hashedToken)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authTokenService.validateToken(rawToken, type)
        );
        assertEquals("Token expiré.", exception.getMessage(), "Message d'erreur incorrect pour token expiré.");
    }

    /**
     * Teste la validation d'un token dont le type ne correspond pas au type attendu.
     * S'attend à une IllegalStateException avec un message spécifique.
     */
    @Test
    void act_validateToken_assert_shouldThrowIllegalState_whenTokenTypeIsWrong() {
        // Arrange
        String rawToken = "token-avec-mauvais-type-test";
        String hashedToken = "hash-mauvais-type";
        TypeAuthTokenTemp typeDansLeToken = TypeAuthTokenTemp.VALIDATION_EMAIL;
        TypeAuthTokenTemp typeAttenduParTest = TypeAuthTokenTemp.RESET_PASSWORD;

        AuthTokenTemporaire tokenAvecMauvaisType = AuthTokenTemporaire.builder()
                .tokenHache(hashedToken)
                .typeToken(typeDansLeToken)
                .utilisateur(utilisateurTest)
                .dateExpiration(LocalDateTime.now().plusHours(1)) // Non expiré
                .isUsed(false) // Non utilisé
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(tokenAvecMauvaisType));
        when(passwordEncoder.matches(rawToken, hashedToken)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                authTokenService.validateToken(rawToken, typeAttenduParTest)
        );
        String expectedMessageStart = "Type de token incorrect.";
        assertTrue(exception.getMessage().startsWith(expectedMessageStart), "Le message d'exception devrait commencer par: '" + expectedMessageStart + "'. Message actuel: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Attendu: " + typeAttenduParTest), "Le message devrait contenir le type attendu.");
        assertTrue(exception.getMessage().contains("Obtenu: " + typeDansLeToken), "Le message devrait contenir le type obtenu.");
    }

    /**
     * Teste la méthode markAsUsed.
     * Vérifie que le token est bien marqué comme utilisé et sauvegardé.
     * C'est ici que l'erreur "Tentative de marquer comme utilisé un token non trouvé en base" se produisait.
     */
    @Test
    void act_markAsUsed_assert_shouldUpdateTokenAndSave() {
        // Arrange
        UUID tokenId = UUID.randomUUID(); // ID unique pour ce token de test.

        // Créer un token simulé qui existerait en base de données AVANT d'être marqué comme utilisé.
        // L'utilisateur est déjà initialisé dans setUp() avec un email et un ID.
        AuthTokenTemporaire tokenExistantEnBaseSimule = AuthTokenTemporaire.builder()
                .idTokenTemp(tokenId)
                .utilisateur(utilisateurTest) // Utilisateur de test initialisé
                .tokenHache("un-hash-quelconque-pour-markasused")
                .typeToken(TypeAuthTokenTemp.RESET_PASSWORD)
                .dateExpiration(LocalDateTime.now().plusHours(1))
                .isUsed(false) // État initial : non utilisé
                .build();

        // **CORRECTION CRUCIALE :**
        // Simuler la méthode findById du repository. Lorsqu'elle est appelée avec tokenId,
        // elle doit retourner notre 'tokenExistantEnBaseSimule' enveloppé dans un Optional.
        // C'est cette simulation qui manquait et causait l'IllegalArgumentException.
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenExistantEnBaseSimule));

        // Capturer l'argument qui sera passé à tokenRepository.save() pour vérification.
        ArgumentCaptor<AuthTokenTemporaire> tokenCaptor = ArgumentCaptor.forClass(AuthTokenTemporaire.class);
        // Optionnel : Simuler la méthode save pour qu'elle retourne l'argument qu'elle reçoit (comportement typique).
        when(tokenRepository.save(any(AuthTokenTemporaire.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // L'objet token passé en paramètre à authTokenService.markAsUsed()
        // n'a besoin que de l'ID pour que le service puisse le retrouver via findById.
        AuthTokenTemporaire tokenParametrePourService = new AuthTokenTemporaire();
        tokenParametrePourService.setIdTokenTemp(tokenId);
        // Il n'est pas nécessaire de setter les autres champs ici, car le service
        // va opérer sur l'instance 'tokenExistantEnBaseSimule' récupérée du repository.

        // Act : Appel de la méthode à tester
        authTokenService.markAsUsed(tokenParametrePourService);

        // Assert
        // 1. Vérifier que tokenRepository.save a été appelé.
        verify(tokenRepository).save(tokenCaptor.capture());

        // 2. Récupérer le token qui a été passé à la méthode save.
        AuthTokenTemporaire tokenSauvegarde = tokenCaptor.getValue();

        // 3. Vérifier que le token sauvegardé (qui est 'tokenExistantEnBaseSimule' après modification)
        // est maintenant marqué comme utilisé.
        assertTrue(tokenSauvegarde.isUsed(), "Le token sauvegardé devrait être marqué comme 'utilisé'.");
        assertEquals(tokenId, tokenSauvegarde.getIdTokenTemp(), "L'ID du token sauvegardé doit correspondre à l'ID original.");

        // 4. (Assertion redondante mais utile pour la compréhension)
        // Vérifier que l'instance 'tokenExistantEnBaseSimule' (celle que findById a retourné) a bien été modifiée.
        assertTrue(tokenExistantEnBaseSimule.isUsed(), "L'instance de token originale (simulant la DB) devrait aussi être marquée comme 'utilisée'.");
    }

    /**
     * Teste la purge des tokens expirés.
     * Vérifie que la méthode du repository est appelée et que le nombre de tokens supprimés est retourné.
     */
    @Test
    void act_purgeExpiredTokens_assert_shouldCallRepositoryAndReturnDeletedCount() {
        // Arrange
        long expectedDeletedCount = 3L;
        // Simuler que la méthode deleteByDateExpirationBefore du repository retourne 3.
        when(tokenRepository.deleteByDateExpirationBefore(any(LocalDateTime.class))).thenReturn(expectedDeletedCount);

        // Act
        long actualDeletedCount = authTokenService.purgeExpiredTokens();

        // Assert
        assertEquals(expectedDeletedCount, actualDeletedCount, "Le nombre de tokens purgés retourné par le service ne correspond pas.");
        // Vérifier que la méthode de suppression du repository a bien été appelée avec n'importe quelle date.
        verify(tokenRepository).deleteByDateExpirationBefore(any(LocalDateTime.class));
    }
}
