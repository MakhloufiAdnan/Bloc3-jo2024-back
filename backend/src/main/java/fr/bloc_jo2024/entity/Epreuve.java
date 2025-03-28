package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Epreuve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEpreuve;

    @Column(nullable = false, length = 50)
    private String nom;

    @ManyToMany(mappedBy = "epreuves")
    private Set<Evenement> evenements = new HashSet<>();
}
