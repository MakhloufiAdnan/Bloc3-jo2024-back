package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.Authentification;
import fr.bloc_jo2024.entity.Role;
import fr.bloc_jo2024.entity.RoleEnum;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import fr.bloc_jo2024.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    // Injection du bean PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur après avoir haché le mot de passe.
     * Vérifie d'abord si l'email est déjà utilisé.
     *
     * @param email L'email de l'utilisateur.
     * @param password Le mot de passe en clair de l'utilisateur.
     * @param roleName Le rôle de l'utilisateur (ADMIN ou USER).
     * @return L'objet Utilisateur créé et enregistré dans la base de données.
     * @throws IllegalArgumentException Si l'email est déjà utilisé ou si le rôle n'est pas valide.
     */
    @Transactional
    public Utilisateur registerUser(String email, String password, RoleEnum roleName) {

        // Vérifie si l'email est déjà présent dans la base
        if (findByEmail(email) != null) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // Récupère le rôle correspondant à partir de l'énumération
        Role role = roleRepository.findByTypeRole(roleName.name())
                .orElseThrow(() -> new IllegalArgumentException("Rôle non valide."));

        // Crée l'objet Authentification et hache le mot de passe via le PasswordEncoder
        Authentification authentification = new Authentification();
        authentification.setMotPasseHache(passwordEncoder.encode(password));

        // Crée l'utilisateur, associe l'authentification et le rôle, puis l'enregistre
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        utilisateur.setAuthentification(authentification);

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email L'email à rechercher.
     * @return L'utilisateur trouvé ou null s'il n'existe pas.
     */
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    /**
     * Vérifie si un utilisateur existe déjà avec l'email spécifié.
     *
     * @param email L'email à vérifier.
     * @return true si l'email est déjà utilisé, sinon false.
     */
    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }
}


