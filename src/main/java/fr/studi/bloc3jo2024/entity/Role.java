package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TypeRole typeRole;

    // Relation One-to-Many vers l'entité Utilisateur. Un rôle peut être attribué à plusieurs utilisateurs.
    @OneToMany(mappedBy = "role", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Utilisateur> utilisateurs;

    @Override
    public String toString() {
        return "Role{" +
                "idRole=" + idRole +
                ", typeRole=" + typeRole +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        if (idRole == null || role.idRole == null) {
            return false;
        }
        return Objects.equals(idRole, role.idRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRole);
    }
}