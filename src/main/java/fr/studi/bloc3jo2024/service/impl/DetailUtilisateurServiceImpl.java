package fr.studi.bloc3jo2024.service.impl;

import fr.studi.bloc3jo2024.entity.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

// Implémentation de UserDetails pour Spring Security, basée sur l'entité Utilisateur.
public record DetailUtilisateurServiceImpl(Utilisateur utilisateur) implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Retourne les autorités (rôles) de l'utilisateur pour Spring Security.
     * @return Une collection d'autorités (SimpleGrantedAuthority).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = utilisateur.getRole().getTypeRole().name();
        return "ADMIN".equalsIgnoreCase(role)
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Retourne le mot de passe de l'utilisateur (haché).
     * @return Le mot de passe haché de l'utilisateur depuis l'entité Authentification.
     */
    @Override
    public String getPassword() {
        return utilisateur.getAuthentification().getMotPasseHache();
    }

    /**
     * Retourne l'identifiant d'utilisateur
     * @return L'email de l'utilisateur.
     */
    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    /**
     * Indique si le compte de l'utilisateur n'est pas expiré.
     * @return true si le compte n'est pas expiré.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indique si le compte de l'utilisateur n'est pas bloqué.
     * @return true si le compte n'est pas bloqué.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indique si les informations d'identification de l'utilisateur (mot de passe) ne sont pas expirées.
     * @return true si les informations d'identification ne sont pas expirées.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indique si le compte de l'utilisateur est activé (vérifié par email).
     * @return La valeur du champ 'isVerified' de l'entité Utilisateur.
     */
    @Override
    public boolean isEnabled() {
        return utilisateur.isVerified();
    }
}