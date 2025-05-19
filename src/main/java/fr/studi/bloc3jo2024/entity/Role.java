package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    // Type de rôle (ADMIN, USER)
    @Enumerated(EnumType.STRING)
    @Column(name = "type_role",nullable = false, unique = true)
    private TypeRole typeRole;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude // Important
    private Set<Utilisateur> utilisateurs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(idRole, role.idRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRole);
    }

    @Override
    public String toString() {
        return "Role{" +
                "idRole=" + idRole +
                ", typeRole=" + typeRole +
                '}';
    }
}