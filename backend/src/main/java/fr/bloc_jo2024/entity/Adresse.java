package fr.bloc_jo2024.entity;
import fr.bloc_jo2024.entity.Utilisateur;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    private Pays pays;

    @OneToMany(mappedBy = "adresse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Utilisateur> utilisateurs;
}
