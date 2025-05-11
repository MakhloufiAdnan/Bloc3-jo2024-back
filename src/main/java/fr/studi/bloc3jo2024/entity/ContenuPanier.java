package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ContenuPanierId.class)
@Table(name = "contenu_panier")
public class ContenuPanier {

    @Column(name = "quantite_commandee", nullable = false)
    private int quantiteCommandee;

    // Clé étrangère composite référençant l'ID du panier.
    @Id // Champ faisant partie de la clé primaire composite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_panier", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_panier"))
    private Panier panier;

    // Clé étrangère composite référençant l'ID de l'offre.
    @Id // Champ faisant partie de la clé primaire composite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre", nullable = false, foreignKey = @ForeignKey(name = "fk_contenu_panier_offre"))
    private Offre offre; // La méthode getOffre() retournera l'entité Offre liée
}