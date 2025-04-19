package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles",
        indexes = @Index(name = "idx_role_type", columnList = "typeRole"))
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_Role",nullable = false, unique = true)
    private RoleEnum typeRole;

    @OneToMany(mappedBy = "role", cascade = CascadeType.PERSIST)
    private Set<Utilisateur> utilisateurs;
}