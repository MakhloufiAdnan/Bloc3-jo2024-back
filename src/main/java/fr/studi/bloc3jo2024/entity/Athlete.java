package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "athletes")
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_athlete")
    private Long idAthlete;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pays", nullable = false)
    @ToString.Exclude
    private Pays pays;

    @OneToMany(mappedBy = "athlete", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Pratiquer> epreuves;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Athlete athlete = (Athlete) o;
        if (idAthlete == null && athlete.idAthlete == null) return super.equals(o);
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
                '}';
    }
}