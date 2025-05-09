package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailUtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private DetailUtilisateurService detailUtilisateurService;

    @Test
    @DisplayName("loadUserByUsername should return UserDetails if user is found")
    void loadUserByUsername_userFound_shouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        Utilisateur mockUtilisateur = new Utilisateur(); // Create a mock or dummy Utilisateur
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(mockUtilisateur));

        // Act
        UserDetails userDetails = detailUtilisateurService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        verify(utilisateurRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException if user is not found")
    void loadUserByUsername_userNotFound_shouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            detailUtilisateurService.loadUserByUsername(email);
        });

        // Assert
        assertTrue(exception.getMessage().contains("Utilisateur non trouvé : " + email));
        verify(utilisateurRepository, times(1)).findByEmail(email);
    }
}