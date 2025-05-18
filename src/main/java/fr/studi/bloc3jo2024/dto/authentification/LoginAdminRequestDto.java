package fr.studi.bloc3jo2024.dto.authentification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginAdminRequestDto {

    /**
     * L'adresse email de l'administrateur.
     * Doit être une adresse email valide et ne peut pas être vide.
     * La validation @Email vérifie le format de l'adresse.
     * La validation @NotBlank s'assure que le champ n'est ni nul ni vide.
     */
    @Email(message = "L'adresse email doit être valide.")
    @NotBlank(message = "L'email est requis.")
    private String email;

    /**
     * Le mot de passe de l'administrateur.
     * Ne doit pas être vide.
     * La validation @NotBlank s'assure que le champ n'est ni nul ni vide.
     */
    @NotBlank(message = "Le mot de passe est requis.")
    private String password;

    /**
     * Méthode utilitaire statique (factory method) pour créer facilement une instance de {@code LoginAdminRequestDto}.
     * Permet une instanciation plus expressive et potentiellement plus de flexibilité
     * pour la création d'objets complexes à l'avenir.
     *
     * @param email L'adresse email de l'administrateur.
     * @param password Le mot de passe de l'administrateur.
     * @return Une nouvelle instance de {@code LoginAdminRequestDto} peuplée avec l'email et le mot de passe fournis.
     */
    public static LoginAdminRequestDto from(String email, String password) {
        LoginAdminRequestDto dto = new LoginAdminRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }
}