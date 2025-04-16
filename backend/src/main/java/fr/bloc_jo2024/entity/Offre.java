package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.StatutOffre;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offres")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private double prix;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_offre", nullable = false)
    private StatutOffre statutOffre;

    @ManyToOne
    @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;

    @OneToMany(mappedBy = "offre")
    private Set<Billet> billets;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ContenuPanier> contenuPaniers = new HashSet<>();
}