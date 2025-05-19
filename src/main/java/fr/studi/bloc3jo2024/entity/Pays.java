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
@Table(name = "pays")
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long idPays;

    @Column(name = "nom_pays", nullable = false, length = 100)
    private String nomPays;

    @OneToMany(mappedBy = "pays", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Adresse> adresses;

    // equals et hashCode basés sur l'ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pays pays = (Pays) o;
        if (idPays == null && pays.idPays == null) return super.equals(o);
        return Objects.equals(idPays, pays.idPays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPays);
    }

    @Override
    public String toString() {
        return "Pays{" +
                "idPays=" + idPays +
                ", nomPays='" + nomPays + '\'' +
                '}';
    }
}