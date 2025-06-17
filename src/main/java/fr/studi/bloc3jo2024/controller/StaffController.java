package fr.studi.bloc3jo2024.controller;

import fr.studi.bloc3jo2024.dto.authentification.AuthReponseDto;
import fr.studi.bloc3jo2024.dto.authentification.LoginUtilisateurRequestDto;
import fr.studi.bloc3jo2024.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private static final Logger log = LoggerFactory.getLogger(StaffController.class);
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthReponseDto> login(@RequestBody LoginUtilisateurRequestDto request) {
        log.info("Tentative de connexion du staff : {}", request.getEmail());
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Vérification que l'utilisateur a bien le rôle SCANNER
            boolean isScanner = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_SCANNER"));

            if (!isScanner) {
                log.warn("Tentative de connexion de staff par un non-scanner : {}", request.getEmail());
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new AuthReponseDto(null, "Accès refusé. Ce compte n'a pas les permissions de scan."));
            }

            String jwt = jwtService.generateToken(authentication.getName());
            log.info("Connexion réussie pour le staff : {}", request.getEmail());
            return ResponseEntity.ok(new AuthReponseDto(jwt, "Connexion staff réussie"));

        } catch (BadCredentialsException e) {
            log.warn("Échec de connexion du staff '{}' : Identifiants invalides.", request.getEmail());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthReponseDto(null, "Identifiants invalides."));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la connexion du staff '{}'", request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthReponseDto(null, "Erreur interne du serveur."));
        }
    }
}