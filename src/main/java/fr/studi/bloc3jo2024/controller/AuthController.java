package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.service.JwtService;
import fr.studi.bloc3jo2024.service.UtilisateurService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int DEFAULT_TOKEN_LOG_MAX_LENGTH = 10;

    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(UtilisateurService utilisateurService, AuthenticationManager authManager, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    private String formatTokenForLog(String token) {
        if (token == null) {
            return "[null]";
        }
        if (token.length() > DEFAULT_TOKEN_LOG_MAX_LENGTH) {
            return token.substring(0, DEFAULT_TOKEN_LOG_MAX_LENGTH) + "...";
        }
        return token;
    }

    private String formatEmailForLog(String email) {
        if (email == null || email.isEmpty()) {
            return "[null_or_empty_email]";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            // Affiche les 3 premiers caractères du nom d'utilisateur et le domaine
            return email.substring(0, Math.min(atIndex, 3)) + "***@" + email.substring(atIndex + 1);
        }
        // Si le format de l'email est inattendu, logguer une version masquée
        return "email_format_invalide_pour_log_" + email.hashCode();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthReponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        String emailLog = formatEmailForLog(request.getEmail());
        log.info("Tentative d'inscription pour l'email : {}", emailLog);
        try {
            utilisateurService.registerUser(request);
            log.info("Inscription réussie pour l'email : {}. Un email de confirmation a été envoyé.", emailLog);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, "Inscription réussie. Un email de confirmation a été envoyé."));
        } catch (IllegalArgumentException e) {
            log.warn("Échec de l'inscription pour l'email '{}': {}", emailLog, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'inscription pour l'email '{}'", emailLog, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, "Une erreur interne est survenue lors de l'inscription."));
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String token) {
        String tokenLog = formatTokenForLog(token);
        log.info("Tentative de confirmation de compte avec le token : {}", tokenLog);
        try {
            utilisateurService.confirmUser(token);
            log.info("Compte confirmé avec succès pour le token : {}", tokenLog);
            return ResponseEntity.ok("Compte activé. Vous pouvez désormais vous connecter.");
        } catch (IllegalArgumentException e) {
            log.warn("Échec de la confirmation du compte (token invalide/expiré) pour le token '{}': {}", tokenLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lien de confirmation invalide ou expiré. " + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Échec de la confirmation du compte (déjà activé/lien utilisé) pour le token '{}': {}", tokenLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce compte est déjà activé ou le lien de confirmation a déjà été utilisé. " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la confirmation de compte pour le token '{}'", tokenLog, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur interne est survenue lors de la confirmation du compte.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthReponseDto> login(@Valid @RequestBody LoginUtilisateurRequestDto request) {
        String emailLog = formatEmailForLog(request.getEmail());
        log.info("Tentative de connexion pour l'email : {}", emailLog);
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            String jwt = jwtService.generateToken(authentication.getName());
            log.info("Connexion réussie pour l'email : {}", emailLog);
            return ResponseEntity.ok(new AuthReponseDto(jwt, "Connexion réussie"));
        } catch (BadCredentialsException e) {
            log.warn("Tentative de connexion échouée pour l'email '{}' : Identifiants invalides.", emailLog);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, "Email ou mot de passe invalide."));
        } catch (AuthenticationException e) {
            log.warn("Échec de l'authentification pour l'email '{}' : {}", emailLog, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, "Échec de l'authentification : " + e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la tentative de connexion pour l'email '{}'", emailLog, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthReponseDto(null, "Une erreur interne est survenue lors de la connexion."));
        }
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> passwordResetRequest(@RequestParam String email) {
        String emailLog = formatEmailForLog(email);
        log.info("Demande de réinitialisation de mot de passe reçue pour l'email : {}", emailLog);
        try {
            utilisateurService.requestPasswordReset(email);
            log.info("Traitement de la demande de réinitialisation de mot de passe pour l'email : {}", emailLog);
        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation de mot de passe pour l'email '{}'", emailLog, e);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> passwordReset(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        String tokenLog = formatTokenForLog(token);
        log.info("Tentative de réinitialisation de mot de passe avec le token : {}", tokenLog);
        try {
            utilisateurService.resetPassword(token, newPassword);
            log.info("Mot de passe réinitialisé avec succès pour le token : {}", tokenLog);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Échec de la réinitialisation du mot de passe (token invalide/expiré ou autre) pour le token '{}': {}", tokenLog, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la réinitialisation du mot de passe pour le token '{}'", tokenLog, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}