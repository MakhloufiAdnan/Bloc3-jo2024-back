package fr.studi.bloc3jo2024.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les endpoints de test E2E sécurisés.
 */
@RestController
@RequestMapping("/api/test")
public class TestSecuredController {

    /**
     * Endpoint simple pour tester l'accès à une ressource sécurisée.
     * L'accès à cet endpoint nécessite une authentification valide.
     * De plus, l'utilisateur authentifié doit posséder le rôle 'USER' ou 'ADMIN'.
     * La vérification des rôles est effectuée via l'annotation {@link PreAuthorize}.
     *
     * @return ResponseEntity avec un message de succès si l'accès est autorisé.
     */
    @GetMapping("/secured")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Spring Expression Language (SpEL) pour la vérification des rôles.
    // 'ROLE_USER' ou 'ROLE_ADMIN' est implicitement vérifié.
    public ResponseEntity<String> getSecuredData() {
        // Si l'exécution atteint ce point, cela signifie que :
        // 1. L'utilisateur a été authentifié avec succès.
        // 2. L'utilisateur possède au moins l'un des rôles requis ('USER' ou 'ADMIN').
        return ResponseEntity.ok("Accès à l'endpoint sécurisé (/api/test/secured) réussi !");
    }
}