package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.Authentification;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeRole; // Assuming this is correct based on your code
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List; // Added import for List

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Corrected import to include verifyNoMoreInteractions and others

@ExtendWith(MockitoExtension.class)
class DetailUtilisateurServiceImplTest {

    @Mock
    private Utilisateur utilisateur;
    @Mock
    private Role role;
    @Mock
    private Authentification authentification;

    private DetailUtilisateurServiceImpl detailUtilisateurService;

    @BeforeEach
    void setUp() {
        // Arrange: Common setup - only initialize the service with the mock user
        detailUtilisateurService = new DetailUtilisateurServiceImpl(utilisateur);
    }

    @Test
    @DisplayName("getAuthorities should return ROLE_ADMIN for ADMIN user")
    void getAuthorities_adminUser_shouldReturnRoleAdmin() {
        // Arrange: Mock the specific dependencies needed for this test
        when(utilisateur.getRole()).thenReturn(role);
        when(role.getTypeRole()).thenReturn(TypeRole.ADMIN);

        // Act: Call the method under test
        Collection<? extends GrantedAuthority> authorities = detailUtilisateurService.getAuthorities();

        // Assert: Verify the returned authorities
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        // Use List.of for comparison as DetailUtilisateurServiceImpl returns List.of
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), authorities);
        // Verify interactions
        verify(utilisateur).getRole(); // Verify getRole was called
        verify(role).getTypeRole();   // Verify getTypeRole was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }

    @Test
    @DisplayName("getAuthorities should return ROLE_USER for non-ADMIN user")
    void getAuthorities_regularUser_shouldReturnRoleUser() {
        // Arrange: Mock the specific dependencies needed for this test
        when(utilisateur.getRole()).thenReturn(role);
        when(role.getTypeRole()).thenReturn(TypeRole.USER); // Or any other non-ADMIN role

        // Act: Call the method under test
        Collection<? extends GrantedAuthority> authorities = detailUtilisateurService.getAuthorities();

        // Assert: Verify the returned authorities
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        // Use List.of for comparison
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_USER")), authorities);
        // Verify interactions
        verify(utilisateur).getRole(); // Verify getRole was called
        verify(role).getTypeRole();   // Verify getTypeRole was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }

    @Test
    @DisplayName("getPassword should return the hashed password from Authentification")
    void getPassword_shouldReturnHashedPassword() {
        // Arrange: Mock the specific dependency needed for this test
        String hashedPassword = "hashedPassword123";
        when(utilisateur.getAuthentification()).thenReturn(authentification);
        when(authentification.getMotPasseHache()).thenReturn(hashedPassword);

        // Act: Call the method under test
        String retrievedPassword = detailUtilisateurService.getPassword();

        // Assert: Verify the returned password
        assertNotNull(retrievedPassword);
        assertEquals(hashedPassword, retrievedPassword);
        // Verify interactions
        verify(utilisateur).getAuthentification();      // Verify getAuthentification was called
        verify(authentification).getMotPasseHache(); // Verify getMotPasseHache was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }

    @Test
    @DisplayName("getUsername should return the user's email")
    void getUsername_shouldReturnUserEmail() {
        // Arrange: Define a mock email (only need to mock the user's email getter)
        String userEmail = "test@example.com";
        when(utilisateur.getEmail()).thenReturn(userEmail);

        // Act: Call the method under test
        String retrievedUsername = detailUtilisateurService.getUsername();

        // Assert: Verify the returned username (email)
        assertNotNull(retrievedUsername);
        assertEquals(userEmail, retrievedUsername);
        // Verify interactions
        verify(utilisateur).getEmail(); // Verify getEmail was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }

    @Test
    @DisplayName("isAccountNonExpired should always return true")
    void isAccountNonExpired_shouldReturnTrue() {
        // Arrange: No specific arrangement needed as it always returns true

        // Act: Call the method under test
        boolean isNonExpired = detailUtilisateurService.isAccountNonExpired();

        // Assert: Verify the result
        assertTrue(isNonExpired);
        // Verify no interaction with mocked user is expected
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isAccountNonLocked should always return true")
    void isAccountNonLocked_shouldReturnTrue() {
        // Arrange: No specific arrangement needed as it always returns true

        // Act: Call the method under test
        boolean isNonLocked = detailUtilisateurService.isAccountNonLocked();

        // Assert: Verify the result
        assertTrue(isNonLocked);
        // Verify no interaction with mocked user is expected
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isCredentialsNonExpired should always return true")
    void isCredentialsNonExpired_shouldReturnTrue() {
        // Arrange: No specific arrangement needed as it always returns true

        // Act: Call the method under test
        boolean isCredentialsNonExpired = detailUtilisateurService.isCredentialsNonExpired();

        // Assert: Verify the result
        assertTrue(isCredentialsNonExpired);
        // Verify no interaction with mocked user is expected
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isEnabled should return true if user is verified")
    void isEnabled_userIsVerified_shouldReturnTrue() {
        // Arrange: Mock user as verified (only need to mock the isVerified getter)
        when(utilisateur.isVerified()).thenReturn(true);

        // Act: Call the method under test
        boolean isEnabled = detailUtilisateurService.isEnabled();

        // Assert: Verify the result
        assertTrue(isEnabled);
        // Verify interactions
        verify(utilisateur).isVerified(); // Verify isVerified was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }

    @Test
    @DisplayName("isEnabled should return false if user is not verified")
    void isEnabled_userIsNotVerified_shouldReturnFalse() {
        // Arrange: Mock user as not verified (only need to mock the isVerified getter)
        when(utilisateur.isVerified()).thenReturn(false);

        // Act: Call the method under test
        boolean isEnabled = detailUtilisateurService.isEnabled();

        // Assert: Verify the result
        assertFalse(isEnabled);
        // Verify interactions
        verify(utilisateur).isVerified(); // Verify isVerified was called
        verifyNoMoreInteractions(utilisateur, role, authentification); // Verify no other interactions
    }
}