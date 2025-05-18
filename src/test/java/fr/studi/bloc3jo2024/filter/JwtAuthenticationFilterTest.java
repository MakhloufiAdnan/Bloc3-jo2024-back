package fr.studi.bloc3jo2024.filter;

import fr.studi.bloc3jo2024.service.DetailUtilisateurService;
import fr.studi.bloc3jo2024.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private DetailUtilisateurService detailUtilisateurService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Renommage des constantes en camelCase pour les champs d'instance finaux
    private final String mockTokenValue = "valid.jwt.token";
    private final String mockBearerToken = "Bearer " + mockTokenValue;
    private final String mockEmail = "test@example.com";
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUserDetails = User.builder()
                .username(mockEmail)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenValidTokenProvided_thenAuthenticatesUserAndProceeds() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        when(jwtService.extractEmail(mockTokenValue)).thenReturn(mockEmail);
        when(jwtService.isTokenValid(mockTokenValue, mockEmail)).thenReturn(true);
        when(detailUtilisateurService.loadUserByUsername(mockEmail)).thenReturn(mockUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(mockEmail, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenNoTokenProvided_thenProceedsWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenPresentButNotBearer_thenProceedsWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("NotBearer " + mockTokenValue);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenBearerPrefixPresentButTokenIsBlank_thenProceedsWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenJwtServiceIsTokenValidReturnsFalse_thenProceedsWithoutAuthAndDoesNotSetError() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        when(jwtService.extractEmail(mockTokenValue)).thenReturn(mockEmail);
        when(jwtService.isTokenValid(mockTokenValue, mockEmail)).thenReturn(false); // Token invalide

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt()); // Le filtre ne doit pas définir le statut ici
    }

    // Source pour les tests paramétrés d'exceptions JWT
    private static Stream<Arguments> jwtExceptionSource() {
        return Stream.of(
                Arguments.of(ExpiredJwtException.class, "Token JWT expiré. Veuillez vous reconnecter."),
                Arguments.of(SignatureException.class, "Signature du token JWT invalide."),
                Arguments.of(MalformedJwtException.class, "Token JWT malformé."),
                Arguments.of(UnsupportedJwtException.class, "Token JWT non supporté.")
        );
    }

    @ParameterizedTest
    @MethodSource("jwtExceptionSource")
    void doFilterInternal_whenSpecificJwtException_thenHandlesAndReturns401(Class<? extends JwtException> exceptionClass, String expectedErrorMessage) throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        // Simuler que extractEmail lève l'exception correspondante
        // Pour cela, nous devons construire une instance de l'exception ou la mocker si elle est complexe
        // Ici, on suppose que jwtService.extractEmail la lèvera directement.
        JwtException exceptionToThrow = null;
        try {
            // Tenter de créer une instance simple si possible (ExpiredJwtException a besoin de header/claims)
            if (exceptionClass.equals(ExpiredJwtException.class)) {
                exceptionToThrow = new ExpiredJwtException(null, null, "Expired for test");
            } else {
                exceptionToThrow = exceptionClass.getDeclaredConstructor(String.class).newInstance("Test exception");
            }
        } catch (Exception e) {
            fail("Failed to instantiate exception for test: " + exceptionClass.getName(), e);
        }
        when(jwtService.extractEmail(mockTokenValue)).thenThrow(exceptionToThrow);

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            when(response.getWriter()).thenReturn(printWriter);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertTrue(stringWriter.toString().contains(expectedErrorMessage),
                    "Le message d'erreur '" + stringWriter.toString() + "' ne contient pas '" + expectedErrorMessage + "'");
        }
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenIllegalArgumentExceptionFromJwtService_thenHandlesAndReturns401() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        when(jwtService.extractEmail(mockTokenValue)).thenThrow(new IllegalArgumentException("Test arg exception from service"));

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            when(response.getWriter()).thenReturn(printWriter);
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertTrue(stringWriter.toString().contains("Argument de token JWT invalide ou format incorrect"));
        }
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenUsernameNotFoundException_thenHandlesAndReturns401() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        when(jwtService.extractEmail(mockTokenValue)).thenReturn(mockEmail);
        when(jwtService.isTokenValid(mockTokenValue, mockEmail)).thenReturn(true);
        when(detailUtilisateurService.loadUserByUsername(mockEmail)).thenThrow(new UsernameNotFoundException("Test user not found"));

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            when(response.getWriter()).thenReturn(printWriter);
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertTrue(stringWriter.toString().contains("Utilisateur du token non trouvé."));
        }
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_whenUnexpectedException_thenHandlesAndReturns500() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        when(jwtService.extractEmail(mockTokenValue)).thenThrow(new RuntimeException("Test unexpected error"));

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            when(response.getWriter()).thenReturn(printWriter);
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            assertTrue(stringWriter.toString().contains("Erreur interne lors de la validation de l'authentification."));
        }
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // extractTokenFromRequest est maintenant package-private dans JwtAuthenticationFilter
    @Test
    void extractTokenFromRequest_ValidBearerToken_ReturnsTokenWithoutPrefix() {
        when(request.getHeader("Authorization")).thenReturn(mockBearerToken);
        String extracted = jwtAuthenticationFilter.extractTokenFromRequest(request);
        assertEquals(mockTokenValue, extracted);
    }

    // Création d'un Stream d'arguments pour les tests de extractTokenFromRequest
    private static Stream<Arguments> tokenExtractionFailureCases() {
        return Stream.of(
                Arguments.of("NoHeader", null, null),
                Arguments.of("InvalidPrefix", "Token abc.def.ghi", null),
                Arguments.of("BearerPrefixOnly", "Bearer ", null),
                Arguments.of("BearerPrefixWithSpacesOnly", "Bearer    ", null),
                Arguments.of("EmptyHeader", "", null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tokenExtractionFailureCases")
    void extractTokenFromRequest_InvalidOrMissingToken_ReturnsNull(String caseName, String headerValue, String expectedToken) {
        when(request.getHeader("Authorization")).thenReturn(headerValue);
        String extracted = jwtAuthenticationFilter.extractTokenFromRequest(request);
        assertEquals(expectedToken, extracted);
    }
}