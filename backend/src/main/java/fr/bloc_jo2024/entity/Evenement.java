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
        indexes = {@Index(name = "idx_evenements_date", columnList = "dateEvenement")}
)
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idEvenement;

    @Column(nullable = false)
    private LocalDateTime dateEvenement;

    @Column(nullable = false)
    @Min(value = 0, message = "Il n'y a plus de place disponible.")
    private int nbPlaceDispo;

    @ManyToOne
    @JoinColumn(name = "idAdresse", nullable = false, foreignKey = @ForeignKey(name = "fk_evenement_adresse"))
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

    @PrePersist
    public void verifierDateEvenement() {
        if (this.dateEvenement.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être dans le passé.");
        }
    }

    // Diminue les places restantes
    public void retirerPlaces(int nb) {
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à retirer doit être positif.");
        if (nb > this.nbPlaceDispo) {
            throw new IllegalArgumentException("Pas assez de places disponibles.");
        }
        this.nbPlaceDispo -= nb;
    }

    // Ajoute des places (utile en cas d’annulation d’un panier)
    public void ajouterPlaces(int nb) {
        if (nb <= 0) throw new IllegalArgumentException("Le nombre à ajouter doit être positif.");
        this.nbPlaceDispo += nb;
    }

    // Mise à jour de la date (ex. en cas de reprogrammation)
    public void updateDate(LocalDateTime nouvelleDate) {
        if (nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date ne peut pas être dans le passé.");
        }
        this.dateEvenement = nouvelleDate;
    }
}

