package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Utilisateur;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Utilisateur utilisateur;

    public CustomUserDetails(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public String getPassword() {
        // Retourne le mot de passe haché stocké dans l'objet Authentification de l'utilisateur
        return utilisateur.getAuthentification().getMotPasseHache();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // On suppose que le compte n'expire jamais
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // On suppose que le compte n'est jamais verrouillé
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // On suppose que les identifiants ne sont jamais expirés
    }

    @Override
    public boolean isEnabled() {
        return true; // On suppose que l'utilisateur est toujours activé
    }

    @Override
    public Collection<? extends SimpleGrantedAuthority> getAuthorities() {

        // Retourne l'autorité basée sur le rôle de l'utilisateur
        return List.of(new SimpleGrantedAuthority(utilisateur.getRole().getTypeRole().name()));
    }
}