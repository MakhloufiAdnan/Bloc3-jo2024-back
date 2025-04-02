package fr.bloc_jo2024.dto;

public class AuthResponse {
    private String token;

    public AuthResponse(String token) {

        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

