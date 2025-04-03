package fr.bloc_jo2024.controller;

import fr.bloc_jo2024.entity.RoleEnum;
import fr.bloc_jo2024.dto.RegisterRequest;
import fr.bloc_jo2024.dto.LoginRequest;
import fr.bloc_jo2024.dto.AuthResponse;
import fr.bloc_jo2024.service.UtilisateurService;
import fr.bloc_jo2024.service.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController

// Préfixe commun pour tous les endpoints d'authentification
@RequestMapping("/auth")
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    // Injection des dépendances via le constructeur
    public AuthController(UtilisateurService utilisateurService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Route pour l'inscription d'un nouvel utilisateur.
     * @param request Objet contenant l'email et le mot de passe de l'utilisateur
     * @return Une réponse HTTP avec un message indiquant le succès ou l'échec de l'inscription
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        // Vérifie si l'email est déjà utilisé avant d'inscrire l'utilisateur
        if (utilisateurService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cet email est déjà utilisé.");
        }

        // Définir un rôle par défaut si aucun rôle n'est fourni
        RoleEnum roleEnum = (request.getRole() != null && !request.getRole().isEmpty())
                ? RoleEnum.valueOf(request.getRole().toUpperCase())
                : RoleEnum.USER;  // Rôle par défaut

        // Encode le mot de passe et enregistre l'utilisateur dans la base de données
        utilisateurService.registerUser(request.getEmail(), passwordEncoder.encode(request.getPassword()), roleEnum);

        // Retourne une réponse 201 CREATED avec un message de succès
        return ResponseEntity.status(HttpStatus.CREATED).body("Inscription réussie !");
    }

    /**
     * Route pour la connexion d'un utilisateur.
     * Vérifie les identifiants et retourne un token JWT si la connexion est réussie.
     * @param request Objet contenant l'email et le mot de passe de l'utilisateur
     * @return Un token JWT ou une erreur si la connexion échoue
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {

            // Authentifie l'utilisateur avec les identifiants fournis
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Récupère les détails de l'utilisateur après authentification réussie
            UserDetails user = (UserDetails) authentication.getPrincipal();

            // Génère un token JWT valide pour l'utilisateur
            String token = jwtService.generateToken(user.getUsername());

            // Retourne le token JWT dans la réponse
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException e) {
            // Si l'authentification échoue, retourne une réponse HTTP 401 (Unauthorized)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe invalide.");
        }
    }
}