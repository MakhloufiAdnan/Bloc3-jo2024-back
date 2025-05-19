package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "adresses")
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresse")
    private Long idAdresse;

    @NotNull
    @Column(name = "numero_rue", nullable = false)
    private Integer numeroRue;

    @Column(name = "nom_rue", nullable = false, length = 250)
    private String nomRue;

    @Column(name = "ville", nullable = false, length = 50)
    private String ville;

    @Column(name = "code_postal", nullable = false, length = 10)
    private String codePostal;

    /**
     * Disciplines se déroulant à cette adresse.
     * Une adresse peut accueillir plusieurs disciplines.
     */
    @OneToMany(mappedBy = "adresse", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Discipline> disciplines = new HashSet<>();

    /**
     * Relation vers les utilisateurs associés à cette adresse.
     * Une adresse peut avoir plusieurs utilisateurs, mais chaque utilisateur a une adresse.
     */
    @OneToMany(mappedBy = "adresse", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    /**
     * Pays de l'adresse. Chaque adresse est associée à un pays.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pays", nullable = false, foreignKey = @ForeignKey(name = "fk_adresse_pays"))
    @ToString.Exclude
    private Pays pays;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adresse adresse = (Adresse) o;
        if (idAdresse == null && adresse.idAdresse == null) return super.equals(o);
        return Objects.equals(idAdresse, adresse.idAdresse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAdresse);
    }

    @Override
    public String toString() {
        return "Adresse{" +
                "idAdresse=" + idAdresse +
                ", numeroRue=" + numeroRue +
                ", nomRue='" + nomRue + '\'' +
                ", ville='" + ville + '\'' +
                ", codePostal='" + codePostal + '\'' +
                '}';
    }
}