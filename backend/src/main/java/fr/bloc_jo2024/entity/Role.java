package fr.bloc_jo2024.entity;
import fr.bloc_jo2024.entity.Utilisateur;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleEnum typeRole;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<Utilisateur> utilisateurs;
}

enum RoleEnum {
    ADMIN, USER
}
