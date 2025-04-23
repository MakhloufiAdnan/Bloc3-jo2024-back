package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "adresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresse")
    private Long idAdresse;

    @Column(name = "numero_rue", nullable = false)
    private Integer  numeroRue;

    @Column(name = "nom_rue", nullable = false, length = 50)
    private String nomRue;

    @Column(name = "ville", nullable = false, length = 50)
    private String ville;

    @Column(name = "code_postal", nullable = false, length = 50)
    private String codePostal;

    // Une adresse peut accueillir plusieurs événements.
    @Default
    @OneToMany(mappedBy = "adresse")
    private Set<Evenement> evenements = new HashSet<>();

    // Relation vers les utilisateurs associés à cette adresse.
    @OneToMany(mappedBy = "adresse")
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    // Relation vers le pays associé à cette adresse. Chaque adresse est associée à un pays.
    @ManyToOne
    @JoinColumn(name = "id_pays", nullable = false, foreignKey = @ForeignKey(name = "fk_adresse_pays"))
    private Pays pays;
}