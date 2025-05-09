package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.dto.authentification.RegisterRequestDto;
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
import java.util.UUID;

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

    /**
     * Inscription d'un utilisateur avec création d'adresse, encodage du mot de passe,
     * attribution du rôle, sauvegarde en base et envoi d'un email de confirmation.
     */
    @Transactional
    public void registerUser(RegisterRequestDto req) {
        if (utilisateurRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé.");
        }

        // Gestion du pays
        Pays pays = paysRepository.findByNomPays(req.getCountry())
                .orElseGet(() -> paysRepository.save(Pays.builder()
                        .nomPays(req.getCountry())
                        .build()));

        // Construction de l'adresse
        Adresse adr = Adresse.builder()
                .numeroRue(req.getStreetnumber())
                .nomRue(req.getAddress())
                .codePostal(req.getPostalcode())
                .ville(req.getCity())
                .pays(pays)
                .build();
        Long existingId = adresseService.getIdAdresseSiExistante(adr);
        Adresse finalAdr = (existingId != null)
                ? adresseService.getAdresseById(existingId)
                : adresseService.creerAdresseSiNonExistante(adr);

        // Récupération du rôle utilisateur
        Role role = roleRepository.findByTypeRole(TypeRole.USER)
                .orElseThrow(() -> new IllegalStateException("Rôle USER manquant."));

        // Création de l'entité Authentification
        Authentification auth = Authentification.builder()
                .motPasseHache(passwordEncoder.encode(req.getPassword()))
                .build();

        // Création de l'utilisateur
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

        // Sauvegarde en base
        utilisateurRepository.save(user);

        // Génération et envoi du token de confirmation
        String rawToken = tokenService.createToken(
                user,
                TypeAuthTokenTemp.VALIDATION_EMAIL,
                Duration.ofDays(1)
        );
        sendEmail(
                user.getEmail(),
                user.getPrenom(),
                confirmationBaseUrl + "?token=" + rawToken,
                "Confirmation de votre compte"
        );
    }

    // Confirme un compte utilisateur à partir d’un token temporaire valide.
    @Transactional
    public void confirmUser(String rawToken) {
        var token = tokenService.validateToken(rawToken, TypeAuthTokenTemp.VALIDATION_EMAIL);
        var user = token.getUtilisateur();

        if (user.isVerified()) {
            throw new IllegalStateException("Compte déjà confirmé.");
        }

        user.setVerified(true);
        user.setCleUtilisateur(UUID.randomUUID().toString());
        utilisateurRepository.save(user);

        tokenService.markAsUsed(token);
    }

    // Génère un token de réinitialisation de mot de passe et envoie un email.
    public void requestPasswordReset(String email) {
        var user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur avec cet email."));

        String rawToken = tokenService.createToken(
                user,
                TypeAuthTokenTemp.RESET_PASSWORD,
                Duration.ofHours(1)
        );

        sendEmail(
                user.getEmail(),
                user.getPrenom(),
                resetPasswordBaseUrl + "?token=" + rawToken,
                "Réinitialisation de votre mot de passe"
        );
    }

    // Réinitialise le mot de passe d’un utilisateur à partir d’un token temporaire.
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        var token = tokenService.validateToken(rawToken, TypeAuthTokenTemp.RESET_PASSWORD);
        var user = token.getUtilisateur();

        user.getAuthentification().setMotPasseHache(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);

        tokenService.markAsUsed(token);
    }

    // Envoie un email via JavaMailSender.
    private void sendEmail(String to, String prenom, String link, String subject) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText("Bonjour " + prenom + ",\n\nCliquez sur le lien suivant :\n" + link);
        mailSender.send(msg);
    }
}
