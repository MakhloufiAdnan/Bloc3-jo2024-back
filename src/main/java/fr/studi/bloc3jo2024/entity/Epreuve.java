package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "epreuves", indexes = {
        @Index(name = "idx_epreuve_nom_epreuve", columnList = "nom_epreuve")
})
public class Epreuve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_epreuve")
    private Long idEpreuve;

    @Column(name = "nom_epreuve")
    private String nomEpreuve;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFeatured = false;

    // Relation via l'entité d'association Comporter (association avec l'événement).
    @Builder.Default
    @OneToMany(mappedBy = "epreuve", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comporter> comporters = new HashSet<>();
}