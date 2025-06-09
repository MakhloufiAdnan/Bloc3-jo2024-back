package fr.studi.bloc3jo2024.config;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Génère une clé aléatoire de 256 bits (32 octets)
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded();
        // Encode-la en Base64 URL-safe
        String base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        System.out.println("Votre clé secrète JWT Base64 (HS256): " + base64Key);
    }
}