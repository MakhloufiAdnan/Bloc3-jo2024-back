package fr.bloc_jo2024.service;

import fr.bloc_jo2024.dto.*;
import fr.bloc_jo2024.entity.Adresse;
import fr.bloc_jo2024.entity.Authentification;
import fr.bloc_jo2024.entity.Pays;
import fr.bloc_jo2024.entity.Role;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.enums.RoleEnum;
import fr.bloc_jo2024.repository.PaysRepository;
import fr.bloc_jo2024.repository.RoleRepository;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PaysRepository paysRepository;
    private final AdresseService adresseService;

    /**
     * Vérifie si un utilisateur existe déjà avec cet email.
     * @param email L'email à tester.
     * @return true si un utilisateur existe, false sinon.
     */
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * Enregistre un nouvel utilisateur avec un rôle par défaut 'USER'.
     * Les données viennent du formulaire front sous forme de DTO (RegisterRequest).
     * @param request DTO contenant les infos du formulaire d'inscription
     * @param passwordEncoder Bean de Spring Security pour encoder les mots de passe
     */
    @Transactional
    public void registerUser(RegisterRequest request, PasswordEncoder passwordEncoder) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // Création ou récupération du pays
        Pays pays = paysRepository.findByNom(request.getCountry())
                .orElseGet(() -> {
                    Pays nouveauPays = new Pays();
                    nouveauPays.setNom(request.getCountry());
                    return paysRepository.save(nouveauPays);
                });

        // Création de l'adresse via le service dédié
        Adresse adresse = Adresse.builder()
                .numeroRue(request.getStreetnumber())
                .nomRue(request.getAddress())
                .codePostale(request.getPostalcode())
                .ville(request.getCity())
                .pays(pays)
                .build();
        Adresse savedAdresse = adresseService.creerAdresse(adresse);

        // Récupération du rôle USER depuis la base de données
        Role role = roleRepository.findByTypeRole(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Le rôle USER n'a pas été trouvé dans la base de données."));

        // Construction de l'objet Authentification avec mot de passe encodé
        Authentification authentification = Authentification.builder()
                .token(UUID.randomUUID().toString())
                .dateExpiration(LocalDateTime.now().plusHours(24))
                .motPasseHache(passwordEncoder.encode(request.getPassword()))
                .build();

        // Construction de l'objet Utilisateur à partir du DTO
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getUsername())
                .prenom(request.getFirstname())
                .email(request.getEmail())
                .dateNaissance(request.getDate())
                .adresse(savedAdresse) // Utilisation de l'adresse persistée
                .authentification(authentification)
                .role(role)
                .isVerified(false) // Initialisation du champ isVerified
                .build();

        // Établissement de la relation inverse entre Authentification et Utilisateur
        authentification.setUtilisateur(utilisateur);

        // Sauvegarde de l'utilisateur en base de données (la cascade gérera l'adresse et l'authentification)
        utilisateurRepository.save(utilisateur);
    }
}