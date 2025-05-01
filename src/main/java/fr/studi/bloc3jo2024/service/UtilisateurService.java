package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.RegisterRequestDto;
import fr.studi.bloc3jo2024.entity.*;
import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import fr.studi.bloc3jo2024.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PaysRepository paysRepository;
    private final AdresseService adresseService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AuthTokenTemporaireService tokenService;

    @Value("${app.frontend.confirmation-base-url}")
    private String confirmationBaseUrl;

    @Value("${app.frontend.reset-password-base-url}")
    private String resetPasswordBaseUrl;

    // Inscription et envoi du token de validation d'email
    @Transactional
    public void registerUser(RegisterRequestDto req) {
        if (utilisateurRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé.");
        }
        // Gestion Pays → Adresse
        Pays pays = paysRepository.findByNomPays(req.getCountry())
                .orElseGet(() -> paysRepository.save(new Pays(null, req.getCountry())));
        Adresse adr = Adresse.builder()
                .numeroRue(req.getStreetnumber())
                .nomRue(req.getAddress())
                .codePostal(req.getPostalcode())
                .ville(req.getCity())
                .pays(pays).build();
        Long existsId = adresseService.getIdAdresseSiExistante(adr);
        Adresse finalAdr = existsId != null
                ? adresseService.getAdresseById(existsId)
                : adresseService.creerAdresseSiNonExistante(adr);

        // Création de l’utilisateur
        Role role = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseThrow(() -> new IllegalStateException("Rôle USER manquant."));
        Authentification auth = Authentification.builder()
                .motPasseHache(passwordEncoder.encode(req.getPassword()))
                .build();
        Utilisateur user = Utilisateur.builder()
                .nom(req.getUsername())
                .prenom(req.getFirstname())
                .email(req.getEmail())
                .dateNaissance(req.getDate())
                .adresse(finalAdr)
                .authentification(auth)
                .role(role)
                .isVerified(false)
                .build();
        auth.setUtilisateur(user);
        utilisateurRepository.save(user);

        // Création et envoi du token de validation
        String rawToken = tokenService.createToken(user, TypeAuthTokenTemp.VALIDATION_EMAIL, Duration.ofDays(1));
        sendEmail(user.getEmail(), user.getPrenom(), confirmationBaseUrl + "?token=" + rawToken,
                "Confirmation de votre compte");
    }

    /** Confirmation du compte via token temporaire */
    @Transactional
    public void confirmUser(String rawToken) {
        var token = tokenService.validateToken(rawToken, TypeAuthTokenTemp.VALIDATION_EMAIL);
        var user  = token.getUtilisateur();
        if (user.isVerified()) {
            throw new IllegalStateException("Compte déjà confirmé.");
        }
        user.setVerified(true);
        utilisateurRepository.save(user);

        // Marquage du token utilisé
        tokenService.markAsUsed(token);
    }

    /** Demande de réinitialisation du mot de passe */
    public void requestPasswordReset(String email) {
        var user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur avec cet email."));
        String rawToken = tokenService.createToken(user, TypeAuthTokenTemp.RESET_PASSWORD, Duration.ofHours(1));
        sendEmail(user.getEmail(), user.getPrenom(), resetPasswordBaseUrl + "?token=" + rawToken,
                "Réinitialisation de votre mot de passe");
    }

    /** Réinitialisation effective du mot de passe */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        var token = tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD);
        var user  = token.getUtilisateur();
        user.getAuthentification()
                .setMotPasseHache(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);
        tokenService.markAsUsed(token);
    }

    private void sendEmail(String to, String prenom, String link, String subject) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText("Bonjour " + prenom + ",\n\nCliquez sur le lien suivant :\n" + link);
        mailSender.send(msg);
    }
}
