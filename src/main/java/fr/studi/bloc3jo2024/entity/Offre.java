package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.entity.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "offres", indexes = {
        @Index(name = "idx_offre_statut", columnList = "statut_offre"),
        @Index(name = "idx_offre_date_expiration", columnList = "date_expiration"), // L'index redevient pertinent
        @Index(name = "idx_offre_discipline", columnList = "id_discipline")
})
@Getter
@Setter
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

    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    /**
     * Date d'expiration spécifique à l'offre (pour promotions, etc.).
     * Peut être null, auquel cas l'expiration est dictée par la date de la discipline.
     * Même si elle est définie, l'offre ne peut pas être valide après la date de sa discipline.
     */
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
    private Discipline discipline;

    @ManyToMany(mappedBy = "offres", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Billet> billets = new ArrayList<>();

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private boolean featured = false;

    /**
     * Calcule la date d'expiration effective de l'offre.
     * C'est la date la plus proche entre la date d'expiration propre à l'offre
     * (si elle existe) et la date de la discipline associée.
     * Si l'offre n'a pas de date d'expiration propre, la date de la discipline est utilisée.
     * Nécessite que 'discipline' et 'discipline.dateDiscipline' ne soient pas null.
     *
     * @return La date d'expiration effective, ou null si la discipline ou sa date ne sont pas définies.
     */
    @Transient // Ce n'est pas un champ persisté, mais une logique métier
    public LocalDateTime getEffectiveDateExpiration() {
        if (this.discipline == null || this.discipline.getDateDiscipline() == null) {
            // Cas anormal, la discipline est @JoinColumn(nullable = false)
            // mais sa dateDiscipline pourrait être null si l'entité Discipline le permet.
            // Retourner la date d'expiration de l'offre si elle existe, sinon null (ou lever une exception).
            return this.dateExpiration;
        }

        LocalDateTime disciplineDate = this.discipline.getDateDiscipline();

        if (this.dateExpiration == null) {
            return disciplineDate; // Expiration dictée par la discipline
        }

        // Si les deux dates existent, prendre la plus proche (la première qui arrive)
        return this.dateExpiration.isBefore(disciplineDate) ? this.dateExpiration : disciplineDate;
    }


    @PreUpdate
    @PrePersist
    public void updateStatutOnChange() {
        // Ne pas modifier un statut déjà terminal comme ANNULE
        if (this.statutOffre == StatutOffre.ANNULE) {
            return;
        }

        // Gérer l'épuisement
        if (quantite <= 0) {
            this.statutOffre = StatutOffre.EPUISE;
            return; // Une offre épuisée ne peut pas devenir expirée par la date, elle reste épuisée.
        }

        // Gérer l'expiration par date, uniquement si l'offre est encore DISPONIBLE
        if (this.statutOffre == StatutOffre.DISPONIBLE) {
            LocalDateTime effectiveExpiration = getEffectiveDateExpiration();
            if (effectiveExpiration != null && !effectiveExpiration.toLocalDate().isAfter(LocalDate.now())) {
                // Si la date effective d'expiration est aujourd'hui ou passée
                this.statutOffre = StatutOffre.EXPIRE;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offre offre = (Offre) o;
        if (idOffre == null || offre.idOffre == null) return this == o;
        return Objects.equals(idOffre, offre.idOffre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOffre);
    }

    @Override
    public String toString() {
        return "Offre{" +
                "idOffre=" + idOffre +
                ", typeOffre=" + typeOffre +
                ", quantite=" + quantite +
                ", prix=" + prix +
                ", dateExpirationPersisted=" + dateExpiration + // Date explicite de l'offre
                ", statutOffre=" + statutOffre +
                ", capacite=" + capacite +
                ", featured=" + featured +
                ", version=" + version +
                (discipline != null ? ", disciplineId=" + discipline.getIdDiscipline() + ", dateDiscipline=" + discipline.getDateDiscipline() : ", disciplineId=null") +
                '}';
    }
}