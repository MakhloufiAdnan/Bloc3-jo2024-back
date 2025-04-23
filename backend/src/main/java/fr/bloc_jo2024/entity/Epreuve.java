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
@Table(name = "epreuves", indexes = {
        @Index(name = "idx_epreuve_nom", columnList = "nom")
})
public class Epreuve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_epreuve")
    private Long idEpreuve;

    @Column(name = "nom_epreuve", nullable = false, length = 50)
    private String nomEpreuve;

    // Relation via l'entité d'association Comporter (association avec l'événement).
    @Builder.Default
    @OneToMany(mappedBy = "epreuve", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comporter> comporters = new HashSet<>();
}