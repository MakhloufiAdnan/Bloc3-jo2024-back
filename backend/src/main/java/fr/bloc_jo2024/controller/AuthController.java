package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.dto.AuthResponse;
import fr.bloc_jo2024.dto.RegisterRequest;
import fr.bloc_jo2024.dto.UserLoginRequestDTO;
import fr.bloc_jo2024.service.JwtService;
import fr.bloc_jo2024.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UtilisateurService utilisateurService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        if (utilisateurService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, "Cet email est déjà utilisé."));
        }

        // Force le rôle USER même si le front essaie de contourner
        request.setRole("USER");

        utilisateurService.registerUser(request, passwordEncoder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(null, "Inscription réussie !"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(user.getUsername());

            return ResponseEntity.ok(new AuthResponse(token, "Connexion réussie"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Email ou mot de passe invalide."));
        }
    }
}