package fr.studi.bloc3jo2024.dto.authentification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {

    @NotBlank(message = "Le token de réinitialisation est requis.")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est requis.")
    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères.")
    private String newPassword;
}
