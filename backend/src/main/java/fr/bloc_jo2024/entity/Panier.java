package fr.bloc_jo2024.entity;
import fr.bloc_jo2024.entity.Utilisateur;
import fr.bloc_jo2024.entity.Offre;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Panier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPanier;

    @Column(nullable = false)
    private double montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPanier statut;

    @Column(nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Offre> offres = new HashSet<>();

    // Méthode pour recalculer le montant total
    public void recalculerMontantTotal() {
        double total = 0.0;
        for (Offre offre : offres) {
            total += offre.getPrix() * offre.getQuantite();
        }
        this.montantTotal = total; // Met à jour le montant total
    }

    // Méthode pour ajouter une offre au panier
    public void ajouterOffre(Offre offre) {
        offres.add(offre);
        recalculerMontantTotal();
    }

    // Méthode pour retirer une offre du panier
    public void retirerOffre(Offre offre) {
        offres.remove(offre);
        recalculerMontantTotal();
    }

    // Méthode pour définir le montant total
    public void setMontantTotal(double montantTotal) {
        if (montantTotal < 0) {
            throw new IllegalArgumentException("Le montant total ne peut pas être négatif.");
        }
        this.montantTotal = montantTotal;
    }
}

enum StatutPanier {
    EN_ATTENTE,
    PAYE,
    SAUVEGARDE
}
