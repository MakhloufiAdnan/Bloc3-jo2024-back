package fr.bloc_jo2024.entity;

import fr.bloc_jo2024.entity.enums.TelEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "telephones")
public class Telephone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TelEnum typeTel;

    @Column(nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Numéro de téléphone invalide")
    private String numeroTelephone;

    @ManyToOne
    @JoinColumn(name = "idUtilisateur", referencedColumnName = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_telephone"))
    private Utilisateur utilisateur;
}