package fr.bloc_jo2024.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * DTO pour la connexion administrateur.
 */
@Data
public class AdminLoginRequestDTO {

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}