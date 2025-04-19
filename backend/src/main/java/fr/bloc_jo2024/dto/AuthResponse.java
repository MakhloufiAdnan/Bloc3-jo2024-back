package fr.bloc_jo2024.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour la réponse d'authentification.
 * Utilisé pour encapsuler le token JWT généré lors d'une connexion réussie
 * et un message informatif (succès ou erreur).
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    // Token JWT pour la connexion réussie. Sera null en cas d'échec.
    private String token;

    // Message d'erreur ou de succès concernant l'opération d'authentification.
    private String message;
}