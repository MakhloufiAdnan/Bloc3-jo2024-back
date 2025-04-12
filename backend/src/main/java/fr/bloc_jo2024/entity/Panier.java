package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.StatutPanier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
    private Set<Offre> offres = new HashSet<>(); // Liste des offres associées au panier

    // Permet de gérer la date automatiquement
    @PrePersist
    public void prePersist() {
        if (dateAjout == null) dateAjout = LocalDateTime.now();
    }
}

