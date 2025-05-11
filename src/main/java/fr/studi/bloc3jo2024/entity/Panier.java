package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.StatutPanier;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
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
    private StatutPanier statut;

    @Column(name = "date_ajout", nullable = false)
    @Builder.Default
    private LocalDateTime dateAjout = LocalDateTime.now();

    // Relation Many-to-One vers l'entité Utilisateur. Chaque panier appartient à un utilisateur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    @EqualsAndHashCode.Exclude // Excluez la relation ManyToOne
    private Utilisateur utilisateur;

    // Relation One-to-Many vers l'entité ContenuPanier (table d'association avec Offre).
    @Builder.Default
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();

    // Relation One-to-One vers l'entité Payement. Un panier peut être associé à un paiement.
    @OneToOne(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Paiement paiement;

    @Version
    private Long version;

    // Méthode exécutée avant la persistance pour s'assurer que la date d'ajout est initialisée.
    @PrePersist
    public void prePersist() {
        if (dateAjout == null) dateAjout = LocalDateTime.now();
    }
}