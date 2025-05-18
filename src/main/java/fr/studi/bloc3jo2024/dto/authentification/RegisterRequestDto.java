package fr.studi.bloc3jo2024.dto.authentification;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Nom d'utilisateur requis")
    private String username;

    @NotBlank(message = "Prénom requis")
    private String firstname;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate date;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;

    @NotBlank(message = "Numéro de téléphone requis")
    private String phonenumber;

    @NotNull(message = "Le numéro de rue est requis (mettre 0 si absent)")
    private Integer streetnumber;

    @NotBlank(message = "Adresse requise")
    private String address;

    @NotBlank(message = "Code postal requis")
    private String postalcode;

    @NotBlank(message = "Ville requise")
    private String city;

    @NotBlank(message = "Mot de passe requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    private String role = "USER"; // Role par défaut

    @NotBlank(message = "Pays requis")
    private String country;
}