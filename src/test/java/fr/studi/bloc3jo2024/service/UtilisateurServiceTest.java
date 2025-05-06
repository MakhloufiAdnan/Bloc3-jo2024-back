package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilisateurServiceTest {

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PaysRepository paysRepository;
    @Mock private AdresseService adresseService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JavaMailSender mailSender;
    @Mock private AuthTokenTemporaireService tokenService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        utilisateurService = new UtilisateurService(
                utilisateurRepository, roleRepository, paysRepository,
                adresseService, passwordEncoder, mailSender, tokenService
        );

        // Mock des URLs de configuration
        org.springframework.test.util.ReflectionTestUtils.setField(utilisateurService, "confirmationBaseUrl", "http://localhost/confirm");
        org.springframework.test.util.ReflectionTestUtils.setField(utilisateurService, "resetPasswordBaseUrl", "http://localhost/reset");
    }
    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Test :registerUser
    @Test
    void registerUser_whenEmailExists_throwsException() {
        // Arrange
        RegisterRequestDto dto = getFakeRegisterDto();
        when(utilisateurRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> utilisateurService.registerUser(dto));
        verify(utilisateurRepository).existsByEmail(dto.getEmail());
    }

    @Test
    void registerUser_success_savesUserAndSendsEmail() {
        // Arrange
        RegisterRequestDto dto = getFakeRegisterDto();

        when(utilisateurRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(paysRepository.findByNomPays(dto.getCountry())).thenReturn(Optional.empty());
        when(paysRepository.save(any())).thenReturn(new Pays());
        when(adresseService.getIdAdresseSiExistante(any())).thenReturn(null);
        when(adresseService.creerAdresseSiNonExistante(any())).thenReturn(new Adresse());
        when(roleRepository.findByTypeRole(TypeRole.USER)).thenReturn(Optional.of(new Role()));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-password");
        when(tokenService.createToken(any(), eq(TypeAuthTokenTemp.VALIDATION_EMAIL), any())).thenReturn("generated-token");

        // Act
        utilisateurService.registerUser(dto);

        // Assert
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // test: confirmUser
    @Test
    void confirmUser_whenAlreadyVerified_throwsException() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().isVerified(true).build();
        AuthTokenTemporaire token = AuthTokenTemporaire.builder().utilisateur(utilisateur).build();

        when(tokenService.validateToken("valid-token", TypeAuthTokenTemp.VALIDATION_EMAIL)).thenReturn(token);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> utilisateurService.confirmUser("valid-token"));
    }

    @Test
    void confirmUser_success_setsVerifiedAndSavesUser() {
        // Arrange
        Utilisateur utilisateur = Utilisateur.builder().isVerified(false).build();
        AuthTokenTemporaire token = AuthTokenTemporaire.builder().utilisateur(utilisateur).build();

        when(tokenService.validateToken("valid-token", TypeAuthTokenTemp.VALIDATION_EMAIL)).thenReturn(token);

        // Act
        utilisateurService.confirmUser("valid-token");

        // Assert
        assertTrue(utilisateur.isVerified());
        verify(utilisateurRepository).save(utilisateur);
        verify(tokenService).markAsUsed(token);
    }

    // Test : requestPasswordReset
    @Test
    void requestPasswordReset_whenUserNotFound_throwsException() {
        // Arrange
        when(utilisateurRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.requestPasswordReset("unknown@example.com"));
    }

    @Test
    void requestPasswordReset_success_sendsEmail() {
        // Arrange
        Utilisateur user = Utilisateur.builder().email("user@example.com").prenom("Alice").build();
        when(utilisateurRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.createToken(any(), eq(TypeAuthTokenTemp.RESET_PASSWORD), any())).thenReturn("reset-token");

        // Act
        utilisateurService.requestPasswordReset(user.getEmail());

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // Test : resetPassword
    @Test
    void resetPassword_success_updatesPasswordAndMarksToken() {
        // Arrange
        Authentification auth = Authentification.builder().build();
        Utilisateur user = Utilisateur.builder().authentification(auth).build();
        AuthTokenTemporaire token = AuthTokenTemporaire.builder().utilisateur(user).build();

        when(tokenService.validateToken("reset-token", TypeAuthTokenTemp.RESET_PASSWORD)).thenReturn(token);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        // Act
        utilisateurService.resetPassword("reset-token", "newpass");

        // Assert
        assertEquals("encoded", user.getAuthentification().getMotPasseHache());
        verify(utilisateurRepository).save(user);
        verify(tokenService).markAsUsed(token);
    }

    private RegisterRequestDto getFakeRegisterDto() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        dto.setUsername("Studi");
        dto.setFirstname("Bob");
        dto.setPassword("MotDePasseUtilisateur123");
        dto.setAddress("La foret");
        dto.setStreetnumber(12);
        dto.setPostalcode("75000");
        dto.setCity("Paris");
        dto.setCountry("France");
        dto.setDate(LocalDate.of(1990, 1, 1));
        return dto;
    }
}
