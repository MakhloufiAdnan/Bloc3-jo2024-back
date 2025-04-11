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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idOffre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOffre typeOffre;

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
}