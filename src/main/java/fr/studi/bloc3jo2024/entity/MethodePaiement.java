package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Objects;

@Entity
@Table(name = "methodes_paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MethodePaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_methode")
    private Long idMethode;

    // Nom de la méthode de paiement (CB, PAYPAL, STRIP), doit être unique.
    @Enumerated(EnumType.STRING)
    @Column(name = "nom_methode_paiement", nullable = false, unique = true, length = 50)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MethodePaiementEnum nomMethodePaiement;

    @Override
    public String toString() {
        return "MethodePaiement{" +
                "idMethode=" + idMethode +
                ", nomMethodePaiement=" + nomMethodePaiement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodePaiement that)) return false;
        return idMethode != null && idMethode.equals(that.idMethode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMethode);
    }
}