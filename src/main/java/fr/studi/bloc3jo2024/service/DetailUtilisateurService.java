package fr.studi.bloc3jo2024.service;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import fr.studi.bloc3jo2024.repository.UtilisateurRepository;
import fr.studi.bloc3jo2024.service.impl.DetailUtilisateurServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetailUtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));

        return new DetailUtilisateurServiceImpl(utilisateur);
    }
}
