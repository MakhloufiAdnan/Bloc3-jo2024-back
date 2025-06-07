package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
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

    @Column(name = "nom_epreuve", nullable = false, unique = true, length = 100)
    private String nomEpreuve;

    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFeatured = false;

    // Relation via l'entité d'association Comporter (association avec l'événement).
    @Builder.Default
    @OneToMany(mappedBy = "epreuve", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comporter> comporters = new HashSet<>();

    @Override
    public String toString() {
        return "Epreuve{" +
                "idEpreuve=" + idEpreuve +
                ", nomEpreuve='" + nomEpreuve + '\'' +
                ", isFeatured=" + isFeatured +
                '}';
    }

    @Override
    public int hashCode() {
        return (idEpreuve != null) ? idEpreuve.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof Epreuve epreuve)) return false;
         return idEpreuve != null && idEpreuve.equals(epreuve.idEpreuve);
    }
}