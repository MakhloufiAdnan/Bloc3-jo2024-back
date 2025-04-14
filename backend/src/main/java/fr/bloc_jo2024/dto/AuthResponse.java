package fr.bloc_jo2024.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    // Token JWT pour la connexion réussie
    private String token;

    // Message d'erreur ou de succès
    private String message;

    public AuthResponse(String token) {
        this.token = token;
        this.message = null;
    }
}