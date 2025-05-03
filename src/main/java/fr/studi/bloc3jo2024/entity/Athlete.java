package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "athletes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Athlete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_athlete")
    private Long idAthlete;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;
    @ManyToOne
    @JoinColumn(name = "id_pays", nullable = false)
    private Pays pays;

    @OneToMany(mappedBy = "athlete")
    private Set<Pratiquer> epreuves;
}
