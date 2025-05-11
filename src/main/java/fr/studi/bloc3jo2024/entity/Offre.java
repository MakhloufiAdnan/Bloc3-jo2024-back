package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type_offre", nullable = false)
    private TypeOffre typeOffre;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix", nullable = false)
    private BigDecimal prix;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_offre", nullable = false)
    private StatutOffre statutOffre;

    @Column(name = "capacite", nullable = false)
    private int capacite;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_discipline", nullable = false, foreignKey = @ForeignKey(name = "fk_offre_discipline"))
    @EqualsAndHashCode.Exclude // Exclu de la relation ManyToOne la discipline dans equals/hashCode de Offre
    private Discipline discipline;

    // Relation Many-to-Many vers l'entité Billet.
    @ManyToMany(mappedBy = "offres")
    @EqualsAndHashCode.Exclude
    private List<Billet> billets;

    @PreUpdate
    @PrePersist
    public void updateStatutOnChange() {
        if (quantite == 0) statutOffre = StatutOffre.EPUISE;
        if (dateExpiration != null && dateExpiration.isBefore(LocalDateTime.now())&& statutOffre != StatutOffre.ANNULE && statutOffre != StatutOffre.EPUISE) {
            statutOffre = StatutOffre.EXPIRE;
        }
    }

    // Relation One-to-Many vers l'entité ContenuPanier.
    @Builder.Default
    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();
}