package fr.studi.bloc3jo2024.dto.authentification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginAdminRequestDto {

    // Email de l'administrateur, doit être une adresse email valide et ne pas être vide.
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    // Mot de passe de l'administrateur, ne doit pas être vide.
    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}
