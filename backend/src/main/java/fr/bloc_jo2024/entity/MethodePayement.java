package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.MethodePayementEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "methodes_payement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodePayement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_methode")
    private Long idMethode;

    // Nom de la méthode de paiement (CB, PAYPAL, STRIP), doit être unique.
    @Enumerated(EnumType.STRING)
    @Column(name = "nom_methode_payement", nullable = false, unique = true, length = 50)
    private MethodePayementEnum nomMethodePayement;
}