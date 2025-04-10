package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Authentification;
import fr.bloc_jo2024.entity.Role;
import fr.bloc_jo2024.entity.RoleEnum;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.exception.ResourceNotFoundException;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     * Pour les inscriptions classiques, on attribue systématiquement le rôle USER.
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe en clair (sera encodé).
     * @param roleName Paramètre ignoré ici (le rôle est fixé à USER par défaut).
     * @return L'utilisateur enregistré.
     */
    @Transactional
    public Utilisateur registerUser(String email, String password, RoleEnum roleName) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // Création du rôle USER en dur
        Role role = new Role();
        role.setTypeRole(RoleEnum.USER); // On assigne directement le rôle USER

        // Création de l'objet Authentification et encodage du mot de passe
        Authentification authentification = new Authentification();
        authentification.setMotPasseHache(passwordEncoder.encode(password));

        // Création de l'utilisateur et affectation des données
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        utilisateur.setAuthentification(authentification);

        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + email));
    }

    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}