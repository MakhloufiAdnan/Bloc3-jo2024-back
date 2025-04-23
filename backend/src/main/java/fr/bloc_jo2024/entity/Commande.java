package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long idCommande;

    @Column(name = "num_commande", nullable = false, unique = true, length = 20)
    private String numCommande;

    // Indique si l'email de confirmation de la commande a été envoyé.
    @Column(name = "envoye_mail", nullable = false)
    private boolean envoyeMail = false;

    // Relation One-to-One vers l'entité Payement. Chaque commande est liée à un paiement unique.
    @OneToOne
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_commande_payement"))
    private Payement payement;
}