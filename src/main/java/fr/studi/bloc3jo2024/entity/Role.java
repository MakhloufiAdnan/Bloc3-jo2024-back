package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles",
        indexes = @Index(name = "idx_role_type", columnList = "type_role"))
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    // Type de rôle (ADMIN, USER)
    @Enumerated(EnumType.STRING)
    @Column(name = "type_role",nullable = false, unique = true)
    private TypeRole typeRole;

    // Relation One-to-Many vers l'entité Utilisateur. Un rôle peut être attribué à plusieurs utilisateurs.
    @OneToMany(mappedBy = "role", cascade = CascadeType.PERSIST)
    private Set<Utilisateur> utilisateurs;
}