package fr.studi.bloc3jo2024.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * DTO pour les utilisateurs classiques qui se connectent via le formulaire frontend.
 */
@Data

public class LoginUtilisateurRequestDto {
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}