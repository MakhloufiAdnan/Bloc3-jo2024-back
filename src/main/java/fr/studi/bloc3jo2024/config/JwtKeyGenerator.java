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
        // Exemple de sortie: "rD/zBwN2A5yC7xQ3W+7G8Y9a0b1c2d3e4f5g6h7i8j9k0l1m2n3o4p5q6r7s8t9u0v1w2x3y4z="
    }
}