package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "athletes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Athlete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_athlete")
    private Long idAthlete;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "genre", length = 10)
    private String genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pays", nullable = false)
    private Pays pays;

    @OneToMany(mappedBy = "athlete")
    @Builder.Default
    private Set<Pratiquer> epreuves = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Athlete athlete)) return false;
        return Objects.equals(idAthlete, athlete.idAthlete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAthlete);
    }

    @Override
    public String toString() {
        return "Athlete{" +
                "idAthlete=" + idAthlete +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", genre='" + genre + '\'' +
                ", paysId=" + (pays != null && pays.getIdPays() != null ? pays.getIdPays() : "null") +
                ", epreuvesCount=" + (epreuves != null ? epreuves.size() : 0) +
                '}';
    }
}
