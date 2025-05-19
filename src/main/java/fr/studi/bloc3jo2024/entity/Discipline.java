package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "disciplines", indexes = {
        @Index(name = "idx_discipline_nom", columnList = "nom_discipline"),
        @Index(name = "idx_discipline_date", columnList = "date_discipline")
})
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_discipline")
    private Long idDiscipline;

    @Column(name = "nom_discipline", nullable = false, length = 100)
    private String nomDiscipline;

    @Column(name = "date_discipline", nullable = false)
    private LocalDateTime dateDiscipline;

    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    @Column(name = "nb_place_dispo", nullable = false)
    private int nbPlaceDispo;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFeatured = false;

    @Version
    private Long version;

    /**
     * Adresse où se déroule la discipline.
     * Relation Many-to-One, chargée en LAZY.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_adresse", nullable = false, foreignKey = @ForeignKey(name = "fk_discipline_adresse"))
    @ToString.Exclude
    private Adresse adresse;

    /**
     * Offres associées à cette discipline.
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    private Set<Offre> offres = new HashSet<>();

    /**
     * Association vers Comporter, liant cette discipline à des épreuves.
     */
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Comporter> comporte = new HashSet<>();

    /**
     * Vérification avant persistance : la date de la discipline ne doit pas être dans le passé.
     */
    @PrePersist
    public void verifierDateDiscipline() {
        if (this.dateDiscipline != null && this.dateDiscipline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de la discipline ne peut pas être dans le passé.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        if (idDiscipline == null && that.idDiscipline == null) return super.equals(o);
        return Objects.equals(idDiscipline, that.idDiscipline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDiscipline);
    }

    @Override
    public String toString() {
        return "Discipline{" +
                "idDiscipline=" + idDiscipline +
                ", nomDiscipline='" + nomDiscipline + '\'' +
                ", dateDiscipline=" + dateDiscipline +
                ", nbPlaceDispo=" + nbPlaceDispo +
                ", isFeatured=" + isFeatured +
                ", version=" + version +
                '}';
    }
}