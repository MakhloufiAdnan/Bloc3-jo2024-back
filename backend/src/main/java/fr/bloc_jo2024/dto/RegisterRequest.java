package fr.bloc_jo2024.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String username;
    private String firstname;
    private LocalDate date;
    private String email;
    private String address;
    private String postalcode;
    private String city;
    private String password;
    private String role= "USER";
    private String country;
}