package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "pays")
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long idPays;

    @Column(name = "nom_pays", nullable = false, unique = true, length = 100)
    private String nomPays;
}