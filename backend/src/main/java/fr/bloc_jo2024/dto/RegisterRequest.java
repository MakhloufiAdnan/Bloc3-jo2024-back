package fr.bloc_jo2024.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nom d'utilisateur requis")
    private String username;

    @NotBlank(message = "Prénom requis")
    private String firstname;

    private LocalDate date;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;

    @NotBlank(message = "Mettre 0 si pas de n° de rue")
    private int streetnumber;

    @NotBlank(message = "Adresse requise")
    private String address;

    @NotBlank(message = "Code postal requis")
    private String postalcode;

    @NotBlank(message = "Ville requise")
    private String city;

    @NotBlank(message = "Mot de passe requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String role = "USER"; // Ne sera jamais pris du front

    @NotBlank(message = "Pays requis")
    private String country;
}