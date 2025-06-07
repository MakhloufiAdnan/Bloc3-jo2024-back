package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Role;
import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetailUtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private DetailUtilisateurService detailUtilisateurService;

    private Utilisateur mockUtilisateur;
    private String emailTest;

    @BeforeEach
    void setUp() {
        emailTest = "test@example.com";
        mockUtilisateur = new Utilisateur();
        mockUtilisateur.setIdUtilisateur(UUID.randomUUID());
        mockUtilisateur.setEmail(emailTest);
        Role mockRole = new Role();
        mockRole.setTypeRole(TypeRole.USER);
        mockUtilisateur.setRole(mockRole);
    }

    @Test
    @DisplayName("loadUserByUsername should return UserDetails if user is found")
    void loadUserByUsername_userFound_shouldReturnUserDetails() {
        // Arrange
        when(utilisateurRepository.findByEmailWithRole(emailTest)).thenReturn(Optional.of(mockUtilisateur));

        // Act
        UserDetails userDetails = detailUtilisateurService.loadUserByUsername(emailTest);

        // Assert
        assertNotNull(userDetails);
        assertEquals(emailTest, userDetails.getUsername());
        verify(utilisateurRepository, times(1)).findByEmailWithRole(emailTest);
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException if user is not found")
    void loadUserByUsername_userNotFound_shouldThrowException() {
        // Arrange
        String emailNonExistant = "nonexistent@example.com";
        when(utilisateurRepository.findByEmailWithRole(emailNonExistant)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
            detailUtilisateurService.loadUserByUsername(emailNonExistant)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Utilisateur non trouvé : " + emailNonExistant));
        verify(utilisateurRepository, times(1)).findByEmailWithRole(emailNonExistant);
    }
}