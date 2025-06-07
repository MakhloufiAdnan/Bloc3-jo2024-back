package fr.studi.bloc3jo2024.entity;


import fr.studi.bloc3jo2024.entity.enums.TypeTel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "telephones")
public class Telephone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telephone")
    private Long idTelephone;

    // Type de téléphone (FIXE, MOBILE)
    @Enumerated(EnumType.STRING)
    @Column(name = "type_tel",nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TypeTel typeTel;

    // Numéro de téléphone, doit être unique et accepté avec l'indicatif international.
    @Column(name = "numero_telephone", nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^(\\+\\d{1,3}[ -]?)?\\(?\\d{1,4}\\)?[ -]?\\d{1,4}[ -]?\\d{1,4}[ -]?\\d{1,4}$", message = "Numéro de téléphone invalide")
    private String numeroTelephone;

    // Relation Many-to-One vers l'entité Utilisateur. Chaque numéro de téléphone appartient à un utilisateur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Telephone telephone = (Telephone) o;
        if (idTelephone == null) return false;
        return idTelephone.equals(telephone.idTelephone);
    }

    @Override
    public int hashCode() {
        return idTelephone != null ? idTelephone.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Telephone{" +
                "idTelephone=" + idTelephone +
                ", typeTel=" + typeTel +
                ", numeroTelephone='" + numeroTelephone + '\'' +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                '}';
    }
}