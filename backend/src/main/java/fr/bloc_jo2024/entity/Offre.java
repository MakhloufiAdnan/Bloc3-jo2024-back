package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOffre;

    @Column(nullable = false)
    private String typeOffre;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false)
    private double prix;

    @Column(nullable = false, unique = true)
    private String qrCode;

    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOffre statutOffre;

    @ManyToOne
    @JoinColumn(name = "idPanier", nullable = false)
    private Panier panier;

    @ManyToOne
    @JoinColumn(name = "idEvenement", nullable = false)
    private Evenement evenement;

    public void setQuantite(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }
        this.quantite = quantite;
    }

    public void setPrix(double prix) {
        if (prix < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }
        this.prix = prix;
    }
}

enum StatutOffre {
    DISPONIBLE,
    EPUISE,
    ANNULE
}