package fr.studi.bloc3jo2024.filter;

import fr.studi.bloc3jo2024.service.DetailUtilisateurService;
import fr.studi.bloc3jo2024.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private DetailUtilisateurService detailUtilisateurService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validToken = "Bearer valid.jwt.token";
    private final String invalidToken = "Bearer invalid.jwt.token";
    private final String testEmail = "test@example.com";
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUserDetails = new User(testEmail, "password", Collections.emptyList());
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_AuthenticatesUserAndContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtService.extractEmail(anyString())).thenReturn(testEmail);
        when(jwtService.isTokenValid(anyString(), eq(testEmail))).thenReturn(true);
        when(detailUtilisateurService.loadUserByUsername(testEmail)).thenReturn(testUserDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testEmail, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_NoToken_ContinuesFilterChainWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidBearerFormat_ContinuesFilterChainWithoutAuthentication() throws ServletException, IOException {
        // Arrange
        String malformedToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(malformedToken);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken_SendsUnauthorizedError() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(invalidToken);
        when(jwtService.extractEmail(anyString())).thenReturn(testEmail);
        when(jwtService.isTokenValid(anyString(), eq(testEmail))).thenReturn(false);

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(writer.toString().contains("Token JWT invalide ou expiré"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ExpiredToken_SendsUnauthorizedError() throws ServletException, IOException {
        // Arrange
        String expiredToken = "Bearer expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(expiredToken);
        when(jwtService.extractEmail(anyString())).thenReturn(testEmail);
        when(jwtService.isTokenValid(anyString(), eq(testEmail))).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(writer.toString().contains("Token expiré"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidSignature_SendsUnauthorizedError() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(invalidToken);
        when(jwtService.extractEmail(anyString())).thenReturn(testEmail);
        when(jwtService.isTokenValid(anyString(), eq(testEmail))).thenThrow(new SignatureException("Invalid signature"));

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(writer.toString().contains("Signature du token invalide"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DetailUtilisateurServiceThrowsException_SendsInternalServerError() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtService.extractEmail(anyString())).thenReturn(testEmail);
        when(jwtService.isTokenValid(anyString(), eq(testEmail))).thenReturn(true);
        when(detailUtilisateurService.loadUserByUsername(testEmail)).thenThrow(new RuntimeException("Database error"));

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertTrue(writer.toString().contains("Une erreur interne est survenue."));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void extractToken_ValidBearerToken_ReturnsTokenWithoutPrefix() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(validToken);

        // Act
        String extracted = jwtAuthenticationFilter.extractToken(request);

        // Assert
        assertEquals("valid.jwt.token", extracted);
    }

    @Test
    void extractToken_NoHeader_ReturnsNull() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        String extracted = jwtAuthenticationFilter.extractToken(request);

        // Assert
        assertNull(extracted);
    }

    @Test
    void extractToken_InvalidPrefix_ReturnsNull() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Token abc.def.ghi");

        // Act
        String extracted = jwtAuthenticationFilter.extractToken(request);

        // Assert
        assertNull(extracted);
    }
}