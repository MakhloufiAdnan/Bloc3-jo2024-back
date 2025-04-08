package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Authentification;
import fr.bloc_jo2024.entity.Role;
import fr.bloc_jo2024.entity.RoleEnum;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.repository.RoleRepository;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Utilisateur registerUser(String email, String password, RoleEnum roleName) {
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // Récupère le rôle dans la BDD. Si non trouvé, une exception est levée (possibilité d'attraper ResourceNotFoundException plus haut)
        Role role = roleRepository.findByTypeRole(roleName.name())
                .orElseThrow(() -> new IllegalArgumentException("Rôle non valide."));

        // Création de l'objet Authentification et hash du mot de passe
        Authentification authentification = new Authentification();
        authentification.setMotPasseHache(passwordEncoder.encode(password));

        // Création de l'objet Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        utilisateur.setAuthentification(authentification);
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}