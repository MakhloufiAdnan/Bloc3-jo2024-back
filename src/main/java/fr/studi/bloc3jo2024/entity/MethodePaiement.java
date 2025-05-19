package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "methodes_paiement", indexes = {
        @Index(name = "idx_methode_paiement_nom", columnList = "nom_methode_paiement", unique = true)
})
public class MethodePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_methode")
    private Long idMethode;

    @NotNull(message = "Le nom de la méthode de paiement ne peut être nul.")
    @Enumerated(EnumType.STRING)
    @Column(name = "nom_methode_paiement", nullable = false, unique = true, length = 50)
    private MethodePaiementEnum nomMethodePaiement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodePaiement that)) return false;
        if (this.idMethode == null || that.idMethode == null) {
            return false;
        }
        return Objects.equals(idMethode, that.idMethode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMethode);
    }

    @Override
    public String toString() {
        return "MethodePaiement{" +
                "idMethode=" + idMethode +
                ", nomMethodePaiement=" + nomMethodePaiement +
                '}';
    }
}