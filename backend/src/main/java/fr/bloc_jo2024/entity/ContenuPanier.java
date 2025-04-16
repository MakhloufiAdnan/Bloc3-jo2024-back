package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenu_panier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ContenuPanierId.class)
public class ContenuPanier {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_panier", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_panier"))
    private Panier panier;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_offre", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_offre"))
    private Offre offre;

    @Column(name = "quantite_commandee", nullable = false)
    private int quantiteCommandee;
}