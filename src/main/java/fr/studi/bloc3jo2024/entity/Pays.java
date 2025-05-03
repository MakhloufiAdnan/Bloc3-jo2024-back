package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "pays")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long idPays;

    @Column(name = "nom_pays", nullable = false, length = 100)
    private String nomPays;

    @OneToMany(mappedBy = "pays")
    private Set<Adresse> adresses;
}