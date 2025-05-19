package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "commandes", indexes = {
        @Index(name = "idx_commande_num_commande", columnList = "num_commande", unique = true),
        @Index(name = "idx_commande_paiement", columnList = "id_payement", unique = true)
})
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long idCommande;

    @NotBlank(message = "Le numéro de commande ne peut pas être vide.")
    @Column(name = "num_commande", nullable = false, unique = true, length = 20)
    private String numCommande;

    @Column(name = "envoye_mail", nullable = false)
    @Builder.Default
    private boolean envoyeMail = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payement", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_commande_payement"))
    @ToString.Exclude
    private Paiement paiement;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commande commande)) return false;
        if (this.idCommande == null || commande.idCommande == null) {
            return false;
        }
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
                (paiement != null ? ", paiementId=" + paiement.getIdPaiement() : ", paiement=null") +
                '}';
    }
}