package fr.bloc_jo2024.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String role;  // Ajouter un champ pour le rôle
}
