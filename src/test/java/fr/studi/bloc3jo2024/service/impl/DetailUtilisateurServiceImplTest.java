package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.Authentification;
import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Arrange:
        detailUtilisateurService = new DetailUtilisateurServiceImpl(utilisateur);
    }

    @Test
    @DisplayName("getAuthorities should return ROLE_ADMIN for ADMIN user")
    void getAuthorities_adminUser_shouldReturnRoleAdmin() {
        // Arrange
        when(utilisateur.getRole()).thenReturn(role);
        when(role.getTypeRole()).thenReturn(TypeRole.ADMIN);

        // Act
        Collection<? extends GrantedAuthority> authorities = detailUtilisateurService.getAuthorities();

        // Assert:
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), authorities);

        verify(utilisateur).getRole();
        verify(role).getTypeRole();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("getAuthorities should return ROLE_USER for non-ADMIN user")
    void getAuthorities_regularUser_shouldReturnRoleUser() {
        // Arrange
        when(utilisateur.getRole()).thenReturn(role);
        when(role.getTypeRole()).thenReturn(TypeRole.USER);

        // Act
        Collection<? extends GrantedAuthority> authorities = detailUtilisateurService.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_USER")), authorities);

        verify(utilisateur).getRole();
        verify(role).getTypeRole();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("getPassword should return the hashed password from Authentification")
    void getPassword_shouldReturnHashedPassword() {
        // Arrange
        String hashedPassword = "hashedPassword123";
        when(utilisateur.getAuthentification()).thenReturn(authentification);
        when(authentification.getMotPasseHache()).thenReturn(hashedPassword);

        // Act
        String retrievedPassword = detailUtilisateurService.getPassword();

        // Assert
        assertNotNull(retrievedPassword);
        assertEquals(hashedPassword, retrievedPassword);

        verify(utilisateur).getAuthentification();
        verify(authentification).getMotPasseHache();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("getUsername should return the user's email")
    void getUsername_shouldReturnUserEmail() {
        // Arrange
        String userEmail = "test@example.com";
        when(utilisateur.getEmail()).thenReturn(userEmail);

        // Act
        String retrievedUsername = detailUtilisateurService.getUsername();

        // Assert
        assertNotNull(retrievedUsername);
        assertEquals(userEmail, retrievedUsername);

        verify(utilisateur).getEmail();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isAccountNonExpired should always return true")
    void isAccountNonExpired_shouldReturnTrue() {
        // Act
        boolean isNonExpired = detailUtilisateurService.isAccountNonExpired();

        // Assert
        assertTrue(isNonExpired);
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isAccountNonLocked should always return true")
    void isAccountNonLocked_shouldReturnTrue() {
        // Act
        boolean isNonLocked = detailUtilisateurService.isAccountNonLocked();

        // Assert
        assertTrue(isNonLocked);
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isCredentialsNonExpired should always return true")
    void isCredentialsNonExpired_shouldReturnTrue() {
        // Act
        boolean isCredentialsNonExpired = detailUtilisateurService.isCredentialsNonExpired();

        // Assert
        assertTrue(isCredentialsNonExpired);
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isEnabled should return true if user is verified")
    void isEnabled_userIsVerified_shouldReturnTrue() {
        // Arrange
        when(utilisateur.isVerified()).thenReturn(true);

        // Act
        boolean isEnabled = detailUtilisateurService.isEnabled();

        // Assert
        assertTrue(isEnabled);

        verify(utilisateur).isVerified();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }

    @Test
    @DisplayName("isEnabled should return false if user is not verified")
    void isEnabled_userIsNotVerified_shouldReturnFalse() {
        // Arrange
        when(utilisateur.isVerified()).thenReturn(false);

        // Act
        boolean isEnabled = detailUtilisateurService.isEnabled();

        // Assert
        assertFalse(isEnabled);

        verify(utilisateur).isVerified();
        verifyNoMoreInteractions(utilisateur, role, authentification);
    }
}
