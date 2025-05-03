package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
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

    @Column(name = "nom_discipline", nullable = false, length = 100)
    private String nomDiscipline;

    @Column(name = "date_discipline", nullable = false)
    private LocalDateTime dateDiscipline;

    // Nombre de places disponibles pour cette discipline.
    @Column(name = "nb_place_dispo", nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    // Chaque discipline pocède une adresse. Une adresse peut accueillir plusieurs épreuves.
    @ManyToOne
    @JoinColumn(name = "id_adresse", nullable = false, foreignKey = @ForeignKey(name = "fk_discipline_adresse"))
    private Adresse adresse;

    /// Relation One-to-Many vers l'entité Offre. La discipline peut avoir plusieurs offres associées.
    @Builder.Default
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Offre> offres = new HashSet<>();

    // Relation vers l'association Comporter qui lie la discipline et une épreuve.
    // Permet de stocker des informations additionnelles (exp. jrDeMedaille).
    @Builder.Default
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comporter> comporters = new HashSet<>();

    // Vérification avant persistance : la date de la discipline ne doit pas être dans le passé.
    @PrePersist
    public void verifierDateDiscipline() {
        if (this.dateDiscipline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de la discipline ne peut pas être dans le passé.");
        }
    }
}