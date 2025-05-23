package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "methodes_paiement")
@Data
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
}