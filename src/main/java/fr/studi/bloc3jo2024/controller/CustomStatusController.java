package fr.studi.bloc3jo2024.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur simple fournissant un endpoint pour vérifier l'état de l'application.
 * Principalement utilisé par Testcontainers pour la stratégie d'attente.
 */
@RestController
public class CustomStatusController {

    /**
     * Endpoint pour vérifier si l'application est démarrée et répond.
     * Renvoie une réponse HTTP 200 OK avec un message simple.
     *
     * @return Une {@link ResponseEntity} indiquant que l'application est opérationnelle.
     */
    @GetMapping("/app-status")
    public ResponseEntity<String> getApplicationStatus() {
        return ResponseEntity.ok("Application is UP and running!");
    }
}