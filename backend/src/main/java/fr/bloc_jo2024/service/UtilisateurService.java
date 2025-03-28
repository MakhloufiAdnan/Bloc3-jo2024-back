package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.Authentification;
import fr.bloc_jo2024.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Méthode pour enregistrer l'utilisateur
    public Utilisateur registerUser(String email, String password) {
        // Création d'une instance d'Authentification pour gérer le mot de passe
        Authentification authentification = new Authentification();
        authentification.setMotPasseHache(passwordEncoder.encode(password));

        // Création de l'utilisateur et association de l'authentification
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setAuthentification(authentification);

        return utilisateurRepository.save(utilisateur);
    }

    // Recherche d'un utilisateur par email
    public Utilisateur findByEmail(String email) {

        return utilisateurRepository.findByEmail(email);
    }
}

