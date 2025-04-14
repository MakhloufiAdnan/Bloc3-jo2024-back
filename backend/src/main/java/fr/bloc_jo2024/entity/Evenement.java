package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "evenements", indexes = {
        @Index(name = "idx_evenements_date", columnList = "dateEvenement")
})
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvenement;

    @Column(nullable = false)
    private LocalDateTime dateEvenement;

    @Column(nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    /**
        Chaque événement pocède une adresse.
        Une adresse peut accueillir plusieurs événements.
     */
    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false, foreignKey = @ForeignKey(name = "fk_evenement_adresse"))
    private Adresse adresse;

    // Relation vers les offres existantes
    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Offre> offres = new HashSet<>();

    // Relation vers l'association Comporter qui lie l'événement et une épreuve.
    // Permet de stocker des informations additionnelles (ex. jrDeMedaille).
    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comporter> comporters = new HashSet<>();

    // Vérification avant persistance : la date de l'événement ne doit pas être dans le passé.
    @PrePersist
    public void verifierDateEvenement() {
        if (this.dateEvenement.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être dans le passé.");
        }
    }
}