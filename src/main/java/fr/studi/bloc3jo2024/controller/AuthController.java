package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
import fr.studi.bloc3jo2024.service.JwtService;
import fr.studi.bloc3jo2024.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthReponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        utilisateurService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthReponseDto(
                        null,
                        "Inscription réussie. Un email de confirmation a été envoyé."
                ));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String token) {
        try {
            utilisateurService.confirmUser(token);
            return ResponseEntity.ok("Compte activé. Vous pouvez désormais vous connecter.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Token invalide ou expiré. " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Compte déjà activé ou lien déjà utilisé. " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthReponseDto> login(@Valid @RequestBody LoginUtilisateurRequestDto request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
            String jwt = jwtService.generateToken(auth.getName());
            return ResponseEntity.ok(new AuthReponseDto(jwt, "Connexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthReponseDto(
                            null,
                            "Email ou mot de passe invalide."
                    ));
        }
    }

    // Déclenche l'envoi d'un email avec un lien de réinitialisation.
    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> passwordResetRequest(@RequestParam String email) {
        utilisateurService.requestPasswordReset(email);
        return ResponseEntity.noContent().build();
    }

    // Effectue la réinitialisation du mot de passe à partir du token et du nouveau mot de passe.
    @PostMapping("/password-reset")
    public ResponseEntity<Void> passwordReset(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        utilisateurService.resetPassword(token, newPassword);
        return ResponseEntity.noContent().build();
    }
}