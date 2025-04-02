package fr.bloc_jo2024.dto;
import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}