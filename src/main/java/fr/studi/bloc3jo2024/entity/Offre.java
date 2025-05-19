package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "offres")
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

    @Builder.Default
    @Column(name = "featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean featured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_discipline", nullable = false, foreignKey = @ForeignKey(name = "fk_offre_discipline"))
    @ToString.Exclude
    private Discipline discipline;

    @ManyToMany(mappedBy = "offres", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Billet> billets = new ArrayList<>();

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();

    @PreUpdate
    @PrePersist
    public void updateStatutOnChange() {
        if (quantite == 0 && statutOffre != StatutOffre.ANNULE) {
            statutOffre = StatutOffre.EPUISE;
        }
        if (dateExpiration != null && dateExpiration.isBefore(LocalDateTime.now()) &&
                statutOffre != StatutOffre.ANNULE && statutOffre != StatutOffre.EPUISE) {
            statutOffre = StatutOffre.EXPIRE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Offre offre)) return false;
        if (this.idOffre == null || offre.idOffre == null) {
            return false;
        }
        return Objects.equals(idOffre, offre.idOffre);
    }

    @Override
    public int hashCode() {
        return idOffre != null ? Objects.hash(idOffre) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Offre{" +
                "idOffre=" + idOffre +
                ", typeOffre=" + typeOffre +
                ", quantite=" + quantite +
                ", prix=" + prix +
                ", dateExpiration=" + dateExpiration +
                ", statutOffre=" + statutOffre +
                ", capacite=" + capacite +
                ", featured=" + featured + // Ajouté
                ", version=" + version +
                (discipline != null ? ", disciplineId=" + discipline.getIdDiscipline() : "") +
                ", nombreBillets=" + (billets != null ? billets.size() : 0) +
                ", nombreContenuPaniers=" + (contenuPaniers != null ? contenuPaniers.size() : 0) +
                '}';
    }
}