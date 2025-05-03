package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    // Type d'offre (SOLO, DUO, FAMILLE).
    @Enumerated(EnumType.STRING)
    @Column(name = "type_offre", nullable = false)
    private TypeOffre typeOffre;

    // Quantité disponible pour cette offre.
    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix", nullable = false)
    private double prix;

    // Date et heure d'expiration de l'offre
    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    // Statut de l'offre (DISPONIBLE, ÉPUISÉ, ANNULÉ).
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_offre", nullable = false)
    private StatutOffre statutOffre;

    // Capacité de l'offre (1 pour SOLO, 2 pour DUO, 4 pour FAMILLE).
    @Column(name = "capacite", nullable = false)
    private int capacite;

    // Relation Many-to-One vers l'entité Evenement. Chaque offre est associée à un événement.
    @ManyToOne
    @JoinColumn(name = "id_evenement", nullable = false, foreignKey = @ForeignKey(name = "fk_offre_evenement"))
    private Discipline discipline;

    // Relation One-to-Many vers l'entité Billet. Une offre peut avoir plusieurs billets vendus.
    @OneToMany(mappedBy = "offre")
    private Set<Billet> billets;

    // Relation One-to-Many vers l'entité ContenuPanier (table d'association avec Panier).
    @Builder.Default
    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();
}