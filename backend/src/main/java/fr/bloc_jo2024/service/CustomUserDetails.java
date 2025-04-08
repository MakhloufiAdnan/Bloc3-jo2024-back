package fr.bloc_jo2024.service;

import fr.bloc_jo2024.entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Utilisateur utilisateur;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Préfixe "ROLE_" nécessaire pour que Spring Security reconnaisse le rôle
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().getTypeRole().name()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getAuthentification().getMotPasseHache();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}