package fr.bloc_jo2024.controller;
import fr.bloc_jo2024.dto.RegisterRequest;
import fr.bloc_jo2024.dto.LoginRequest;
import fr.bloc_jo2024.dto.AuthResponse;
import fr.bloc_jo2024.service.UtilisateurService;
import fr.bloc_jo2024.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Inscription de l'utilisateur avec encodage du mot de passe
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        // Vérifier si l'email est déjà utilisé
        if (utilisateurService.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cette email existe.");
        }

        // Inscrire l'utilisateur avec l'email et le mot de passe encodé
        utilisateurService.registerUser(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body("Inscription réussie !");
    }

    // Connexion de l'utilisateur et génération du token JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            // Authentification de l'utilisateur avec les identifiants fournis
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Récupérer les détails de l'utilisateur après l'authentification réussie
            UserDetails user = (UserDetails) authentication.getPrincipal();

            // Générer un token JWT
            String token = jwtService.generateToken(user.getUsername());

            // Retourner le token JWT dans la réponse
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException e) {

            // Si une erreur d'authentification un message est retourné
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Email ou mot de passe invalide."));
        }
    }
}
