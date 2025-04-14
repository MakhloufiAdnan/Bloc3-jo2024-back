package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "adresses")
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdresse;

    @Column(nullable = false)
    private int numeroRue;

    @Column(nullable = false, length = 50)
    private String nomRue;

    @Column(nullable = false, length = 50)
    private String ville;

    @Column(nullable = false, length = 50)
    private String codePostal;

    // Une adresse peut accueillir plusieurs événements.
    @OneToMany(mappedBy = "adresse", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Evenement> evenements = new HashSet<>();

    // Relation vers les utilisateurs associés à cette adresse.
    @OneToMany(mappedBy = "adresse", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Utilisateur> utilisateurs;

    // Relation vers le pays associé à cette adresse.
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false, foreignKey = @ForeignKey(name = "fk_adresse_pays"))
    private Pays pays;
}