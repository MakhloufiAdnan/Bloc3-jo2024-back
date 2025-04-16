package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "epreuves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Epreuve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_epreuve")
    private Long idEpreuve;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    // Relation via l'entité d'association Comporter (association avec l'événement).
    @OneToMany(mappedBy = "epreuve", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comporter> comporters = new HashSet<>();
}