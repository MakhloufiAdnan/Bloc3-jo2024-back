package fr.bloc_jo2024.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour les utilisateurs classiques qui se connectent via le formulaire frontend.
 */
@Data
public class UserLoginRequestDTO {

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}