package fr.bloc_jo2024.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AdminController {

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private static final String SESSION_ADMIN_LOGGED_IN = "ADMIN_LOGGED_IN";
    private static final String JSON_KEY_MESSAGE = "message";

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(JSON_KEY_MESSAGE, "Email et mot de passe requis."));
        }

        if (email.equals(adminEmail) && password.equals(adminPassword)) {
            HttpSession session = request.getSession();
            session.setAttribute(SESSION_ADMIN_LOGGED_IN, true);

            Map<String, String> response = new HashMap<>();
            response.put("token", "dummy-jwt-token");
            response.put(JSON_KEY_MESSAGE, "Connexion réussie.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, "Identifiants invalides."));
        }
    }

    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkAdminSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_ADMIN_LOGGED_IN));

        return ResponseEntity.ok(Map.of("authenticated", isLoggedIn));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> adminLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, "Déconnexion réussie."));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> adminDashboard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_ADMIN_LOGGED_IN) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, "Non autorisé."));
        }

        return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, "Bienvenue sur le dashboard admin."));
    }
}
