package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeTel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "telephones", indexes = {
        @Index(name = "idx_telephone_numero", columnList = "numero_telephone", unique = true),
        @Index(name = "idx_telephone_utilisateur", columnList = "id_utilisateur_uuid")
})
public class Telephone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telephone")
    private Long idTelephone;

    @NotNull(message = "Le type de téléphone ne peut pas être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_tel", nullable = false)
    private TypeTel typeTel;

    @NotNull(message = "Le numéro de téléphone ne peut pas être nul.")
    @Pattern(regexp = "^(\\+\\d{1,3}[ -]?)?\\(?\\d{1,4}\\)?[ -]?\\d{1,4}[ -]?\\d{1,4}[ -]?\\d{1,4}$", message = "Numéro de téléphone invalide.")
    @Column(name = "numero_telephone", nullable = false, unique = true, length = 20)
    private String numeroTelephone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false, foreignKey = @ForeignKey(name = "fk_telephone_utilisateur"))
    @ToString.Exclude
    private Utilisateur utilisateur;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Telephone that)) return false;
        if (this.idTelephone == null || that.idTelephone == null) {
            return false;
        }
        return Objects.equals(idTelephone, that.idTelephone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTelephone);
    }

    @Override
    public String toString() {
        return "Telephone{" +
                "idTelephone=" + idTelephone +
                ", typeTel=" + typeTel +
                ", numeroTelephone='" + (numeroTelephone != null ? "[protégé]" : "null") + '\'' +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                '}';
    }
}