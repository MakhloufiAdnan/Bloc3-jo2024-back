package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "disciplines", indexes = {
        @Index(name = "idx_discipline_nom", columnList = "nom_discipline"),
        @Index(name = "idx_discipline_date", columnList = "date_discipline")
})
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_discipline")
    private Long idDiscipline;

    @Column(name = "nom_discipline", nullable = false, length = 100 , unique = true)
    private String nomDiscipline;

    @Column(name = "date_discipline", nullable = false)
    private LocalDateTime dateDiscipline;

    @Column(name = "nb_place_dispo", nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFeatured = false;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_adresse", nullable = false, foreignKey = @ForeignKey(name = "fk_discipline_adresse"))
    private Adresse adresse;

    @Builder.Default
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Offre> offres = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comporter> comporters = new HashSet<>();

    @PrePersist
    public void verifierDateDiscipline() {
        if (this.dateDiscipline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de la discipline ne peut pas être dans le passé.");
        }
    }

    @Override
    public String toString() {
        return "Discipline{" +
                "idDiscipline=" + idDiscipline +
                ", nomDiscipline='" + nomDiscipline + '\'' +
                ", dateDiscipline=" + dateDiscipline +
                ", nbPlaceDispo=" + nbPlaceDispo +
                ", isFeatured=" + isFeatured +
                ", adresseId=" + (adresse != null ? adresse.getIdAdresse() : "null") +
                '}';
    }

    @Override
    public int hashCode() {
        return idDiscipline != null ? idDiscipline.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Discipline that)) return false;
        return idDiscipline != null && idDiscipline.equals(that.idDiscipline);
    }
}