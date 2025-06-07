package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "paniers", indexes = {
        @Index(name = "idx_paniers_statut", columnList = "statut_panier")
})
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_panier")
    private Long idPanier;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    // Statut du panier (EN_ATTENTE, PAYE, SAUVEGARDE).
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_panier", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StatutPanier statut;

    @Column(name = "date_ajout", nullable = false)
    @Builder.Default
    private LocalDateTime dateAjout = LocalDateTime.now();

    // Relation Many-to-One vers l'entité Utilisateur. Chaque panier appartient à un utilisateur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    // Relation One-to-Many vers l'entité ContenuPanier (table d'association avec Offre).
    @Builder.Default
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();

    // Relation One-to-One vers l'entité Payement. Un panier peut être associé à un paiement.
    @OneToOne(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Paiement paiement;

    @Version
    private Long version;

    // Méthode exécutée avant la persistance pour s'assurer que la date d'ajout est initialisée.
    @PrePersist
    public void prePersist() {
        if (dateAjout == null) dateAjout = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Panier panier)) return false;
        return Objects.equals(idPanier, panier.idPanier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPanier);
    }

    @Override
    public String toString() {
         return "Panier{" +
                 "idPanier=" + idPanier +
                 ", montantTotal=" + montantTotal +
                 ", statut=" + statut +
                 ", dateAjout=" + dateAjout +
                 (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                 ", contenuPaniersCount=" + (contenuPaniers != null ? contenuPaniers.size() : 0) +
                 (paiement != null ? ", paiementId=" + paiement.getIdPaiement() : "") +
                 ", version=" + version +
                 '}';
    }
}