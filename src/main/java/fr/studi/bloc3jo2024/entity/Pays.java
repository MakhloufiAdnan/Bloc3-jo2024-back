package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "pays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long idPays;

    @Column(name = "nom_pays", nullable = false, length = 100, unique = true)
    private String nomPays;

    @OneToMany(mappedBy = "pays",fetch = FetchType.LAZY)
    private Set<Adresse> adresses;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pays pays)) return false;
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
                (adresses != null ? ", adressesCount=" + adresses.size() : ", adresses=null") +
                '}';
    }
}