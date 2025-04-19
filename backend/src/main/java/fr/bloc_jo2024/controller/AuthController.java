package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.dto.*;
import fr.bloc_jo2024.service.JwtService;
import fr.bloc_jo2024.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion de l'authentification des utilisateurs standard via JWT.
 * Les routes sont préfixées par '/auth'.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint pour l'inscription d'un nouvel utilisateur.
     * Valide les données de la requête (RegisterRequest).
     * Vérifie si l'email est déjà utilisé, force le rôle 'USER' côté backend,
     * enregistre l'utilisateur et renvoie une réponse de succès.
     * @param request Les données d'inscription de l'utilisateur.
     * @return ResponseEntity contenant un message de succès en cas d'inscription réussie,
     * ou une réponse 400 Bad Request si l'email est déjà utilisé.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // Vérifie si un utilisateur avec cet email existe déjà
        if (utilisateurService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, "Cet email est déjà utilisé."));
        }

        // Force le rôle 'USER' côté backend pour éviter toute modification de privilèges non autorisée.
        request.setRole("USER");

        // Enregistre le nouvel utilisateur dans la base de données
        utilisateurService.registerUser(request, passwordEncoder);

        // Renvoie une réponse 201 Created avec un message de succès.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(null, "Inscription réussie !"));
    }

    /**
     * Endpoint pour la connexion d'un utilisateur standard.
     * Valide les identifiants (UserLoginRequestDTO) et tente d'authentifier l'utilisateur
     * via l'AuthenticationManager. En cas de succès, génère un token JWT et le renvoie dans la réponse.
     * @param request Les identifiants de l'utilisateur (email et mot de passe).
     * @return ResponseEntity contenant le token JWT et un message de succès en cas de connexion réussie,
     * ou une réponse 401 Unauthorized en cas d'échec d'authentification.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequestDTO request) {
        try {

            // Tente d'authentifier l'utilisateur avec l'email et le mot de passe fournis.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Récupère les détails de l'utilisateur authentifié
            UserDetails user = (UserDetails) authentication.getPrincipal();

            // Génère un token JWT pour cet utilisateur.
            String token = jwtService.generateToken(user.getUsername());

            // Renvoie une réponse 200 OK avec le token JWT et un message de succès.
            return ResponseEntity.ok(new AuthResponse(token, "Connexion réussie"));

        } catch (Exception e) {

            // En cas d'échec d'authentification, renvoie une réponse 401 Unauthorized avec un message d'erreur.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Email ou mot de passe invalide."));
        }
    }
}