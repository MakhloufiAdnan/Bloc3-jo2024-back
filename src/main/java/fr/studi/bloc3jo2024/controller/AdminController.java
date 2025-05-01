package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.LoginAdminRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour la gestion de l'authentification de l'administrateur via la session HTTP.
 * Les routes sont préfixées par '/api/admin/auth'.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminController {

    // Email de l'administrateur, configuré via la propriété 'admin.email'.
    @Value("${admin.email}")
    private String adminEmail;

    // Mot de passe de l'administrateur, configuré via la propriété 'admin.password'.
    @Value("${admin.password}")
    private String adminPassword;

    // Indiquer qu'un administrateur est connecté.
    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";

    // Clé JSON pour le message de réponse.
    private static final String JSON_KEY_MESSAGE = "message";

    // Clé JSON pour indiquer si l'administrateur est authentifié.
    private static final String JSON_KEY_AUTHENTICATED = "authenticated";

    // Clé JSON pour le token (même si ici j'utilise la session, je garde une clé 'token' par cohérence).
    private static final String JSON_KEY_TOKEN = "token";

    /**
     * Endpoint pour la connexion de l'administrateur.
     * Valide les identifiants fournis dans le corps de la requête (AdminLoginRequestDTO).
     * En cas de succès, enregistre l'état de connexion dans la session HTTP.
     * @param credentials Les identifiants de l'administrateur (email et mot de passe).
     * @param request     La requête HTTP.
     * @return ResponseEntity contenant un message de succès et un token de session en cas de succès,
     * ou une réponse 401 Unauthorized en cas d'échec d'authentification.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody LoginAdminRequestDto credentials, HttpServletRequest request) {
        String email = credentials.getEmail();
        String password = credentials.getPassword();

        // Vérifie si l'email et le mot de passe fournis correspondent aux identifiants de l'administrateur configurés.
        if (email.equals(adminEmail) && password.equals(adminPassword)) {

            // Récupère la session HTTP et y enregistre l'état de connexion de l'administrateur.
            HttpSession session = request.getSession();
            session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

            // Renvoie une réponse 200 OK avec un message de succès et un token de session.
            return ResponseEntity.ok(Map.of(JSON_KEY_TOKEN, "admin-session", JSON_KEY_MESSAGE, "Connexion administrateur réussie."));
        } else {

            //En cas d'échec d'authentification, renvoie une réponse 401 Unauthorized avec un message d'erreur.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, "Identifiants administrateur invalides."));
        }
    }

    /**
     * Endpoint pour vérifier si une session d'administrateur est active.
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un booléen indiquant si l'administrateur est authentifié.
     */
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Boolean>> checkAdminSession(HttpServletRequest request) {

        // Récupère la session HTTP sans la créer si elle n'existe pas.
        HttpSession session = request.getSession(false);

        // Vérifie si la session existe et si l'attribut de connexion de l'administrateur est présent.
        boolean isLoggedIn = session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ADMIN_LOGGED_IN));

        // Renvoie une réponse 200 OK avec l'état d'authentification de l'administrateur.
        return ResponseEntity.ok(Map.of(JSON_KEY_AUTHENTICATED, isLoggedIn));
    }

    /**
     * Endpoint pour la déconnexion de l'administrateur.
     * Invalide la session HTTP actuelle.
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un message de succès.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> adminLogout(HttpServletRequest request) {

        // Récupère la session HTTP sans la créer si elle n'existe pas.
        HttpSession session = request.getSession(false);

        // Si une session existe, l'invalide.
        if (session != null) {
            session.invalidate();
        }

        // Renvoie une réponse 200 OK avec un message de déconnexion réussie.
        return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, "Déconnexion administrateur réussie."));
    }

    /**
     * Endpoint pour accéder au dashboard de l'administrateur (nécessite une session d'administrateur active).
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un message de bienvenue si l'administrateur est connecté,
     * ou une réponse 401 Unauthorized si l'administrateur n'est pas connecté.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> adminDashboard(HttpServletRequest request) {

        // Récupère la session HTTP sans la créer si elle n'existe pas.
        HttpSession session = request.getSession(false);

        // Vérifie si la session existe et si l'attribut de connexion de l'administrateur est présent.
        if (session == null || session.getAttribute(SESSION_ADMIN_LOGGED_IN) == null) {

            // Si l'administrateur n'est pas connecté, renvoie une réponse 401 Unauthorized.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, "Non autorisé."));
        }

        // Si l'administrateur est connecté, renvoie une réponse 200 OK avec un message de bienvenue.
        return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, "Bienvenue José."));
    }
}