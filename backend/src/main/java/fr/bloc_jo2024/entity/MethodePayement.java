package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodePayement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMethode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private MethodePayementEnum nomMethode;
}

enum MethodePayementEnum {
    CARTE_BANCAIRE,
    PAYPAL,
    STRIPE
}