package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long idCommande;

    @Column(name = "num_commande", nullable = false, unique = true, length = 20)
    private String numCommande;

    @Column(name = "envoye_mail", nullable = false)
    private boolean envoyeMail = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_commande_payement"))
    private Paiement paiement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commande commande)) return false;
        return Objects.equals(idCommande, commande.idCommande);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCommande);
    }

    @Override
    public String toString() {
        return "Commande{" +
                "idCommande=" + idCommande +
                ", numCommande='" + numCommande + '\'' +
                ", envoyeMail=" + envoyeMail +
                ", paiementId=" + (paiement != null && paiement.getIdPaiement() != null ? paiement.getIdPaiement() : "null") +
                '}';
    }
}