package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "evenements",
        indexes = {@Index(name = "idx_evenements_date", columnList = "date_evenement")}
)
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvenement;

    @Column(nullable = false)
    private LocalDateTime dateEvenement;

    @Column(nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    @ManyToOne(cascade = CascadeType.PERSIST)  // Pour garantir l'existence d'une adresse
    @JoinColumn(name = "idAdresse", nullable = false)
    private Adresse adresse;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Offre> offres = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "evenement_epreuve",
            joinColumns = @JoinColumn(name = "idEvenement"),
            inverseJoinColumns = @JoinColumn(name = "idEpreuve")
    )
    private Set<Epreuve> epreuves = new HashSet<>();

    // Méthode pour mettre à jour les places disponibles
    public void decrementerPlaces(int nb) {
        if (this.nbPlaceDispo - nb < 0) {
            throw new IllegalArgumentException("Pas assez de places disponibles.");
        }
        this.nbPlaceDispo -= nb;
    }

    // Méthode pour vérifier que la date de l'événement n'est pas dans le passé
    @PrePersist
    public void checkDate() {
        if (this.dateEvenement.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être dans le passé.");
        }
    }
}

