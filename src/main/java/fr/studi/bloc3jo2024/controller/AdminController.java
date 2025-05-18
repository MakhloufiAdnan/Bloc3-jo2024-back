package fr.studi.bloc3jo2024.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.studi.bloc3jo2024.dto.authentification.LoginAdminRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Value("${admin.email}")
    private String configuredAdminEmail;

    @Value("${admin.password}")
    private String configuredAdminPasswordHash;

    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper; // Utilisé pour la sérialisation JSON, notamment pour les réponses d'erreur.

    public static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";

    // Constantes pour les clés JSON utilisées dans les réponses.
    private static final String JSON_KEY_MESSAGE = "message";
    private static final String JSON_KEY_AUTHENTICATED = "authenticated";
    private static final String JSON_KEY_TOKEN = "token";

    /**
     * Constructeur pour l'injection de dépendances.
     * @param passwordEncoder L'encodeur de mot de passe pour la vérification sécurisée des mots de passe.
     * @param objectMapper Pour la sérialisation/désérialisation JSON.
     */
    public AdminController(PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * Endpoint pour la connexion de l'administrateur.
     * Valide les identifiants fournis. En cas de succès, un attribut est positionné dans la session HTTP
     * pour marquer l'administrateur comme connecté.
     *
     * @param credentials Les identifiants de l'administrateur (email et mot de passe en clair).
     * @param request     La requête HTTP pour accéder ou créer la session.
     * @return ResponseEntity contenant un message de succès et un "token" symbolique de session,
     * ou une réponse 401 Unauthorized en cas d'échec.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody LoginAdminRequestDto credentials, HttpServletRequest request) {
        String submittedEmail = credentials.getEmail();
        String submittedPassword = credentials.getPassword();

        log.info("Tentative de connexion administrateur pour l'email : {}", submittedEmail);

        // Vérification SÉCURISÉE des identifiants :
        // 1. Comparaison de l'email.
        // 2. Comparaison du mot de passe soumis (en clair) avec le hash stocké, en utilisant PasswordEncoder.
        if (configuredAdminEmail.equals(submittedEmail) &&
                passwordEncoder.matches(submittedPassword, configuredAdminPasswordHash)) {

            // Création ou récupération de la session existante.
            // true: crée une nouvelle session si aucune n'existe pour cette requête.
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

            log.info("Connexion administrateur réussie pour l'email : {}. ID de session : {}", submittedEmail, session.getId());
            return ResponseEntity.ok(Map.of(
                    JSON_KEY_TOKEN, "admin-session-active", // "token" symbolique pour indiquer une session active.
                    JSON_KEY_MESSAGE, "Connexion administrateur réussie."
            ));
        } else {
            log.warn("Échec de la connexion administrateur pour l'email '{}' : Identifiants invalides ou mot de passe incorrect.", submittedEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, "Identifiants administrateur invalides."));
        }
    }

    /**
     * Endpoint pour vérifier si une session d'administrateur est active et valide.
     *
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un booléen indiquant si l'administrateur est authentifié.
     */
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Boolean>> checkAdminSession(HttpServletRequest request) {
        // Récupère la session HTTP sans la créer si elle n'existe pas (false).
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ADMIN_LOGGED_IN));

        if (isLoggedIn) {
            log.debug("Vérification de session admin : Actif. Session ID : {}", session.getId());
        } else {
            log.debug("Vérification de session admin : Inactif.");
        }
        return ResponseEntity.ok(Map.of(JSON_KEY_AUTHENTICATED, isLoggedIn));
    }

    /**
     * Endpoint pour la déconnexion de l'administrateur.
     * Invalide la session HTTP actuelle si elle existe.
     *
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un message de succès.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> adminLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // Ne pas créer de session si elle n'existe pas.
        String message;
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            message = "Déconnexion administrateur réussie. Session invalidée.";
            log.info("{} Session ID : {}", message, sessionId);
        } else {
            message = "Aucune session administrateur active à déconnecter.";
            log.info(message);
        }
        return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, message));
    }

    /**
     * Endpoint pour accéder au dashboard de l'administrateur.
     * Nécessite une session d'administrateur active.
     * La protection de cet endpoint est gérée manuellement ici en vérifiant l'attribut de session.
     * Si Spring Security est utilisé, il est préférable de sécuriser cet endpoint via la configuration de sécurité
     * (par exemple, en utilisant des filtres ou des annotations @PreAuthorize).
     *
     * @param request La requête HTTP.
     * @return ResponseEntity contenant un message de bienvenue ou une réponse 401 Unauthorized.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> adminDashboard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ADMIN_LOGGED_IN))) {
            log.info("Accès autorisé au dashboard pour l'administrateur (session ID : {})", session.getId());
            // Le message "Bienvenue José." est un placeholder.
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, "Bienvenue sur le dashboard administrateur."));
        } else {
            log.warn("Accès non autorisé au dashboard administrateur : session invalide ou manquante.");
            // Réponse JSON structurée pour l'erreur 401.
            // L'utilisation de objectMapper ici est évitée en retournant directement un ResponseEntity.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(JSON_KEY_MESSAGE, "Non autorisé. Veuillez vous connecter."));
        }
    }
}