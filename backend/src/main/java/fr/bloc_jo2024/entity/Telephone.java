package fr.bloc_jo2024.entity;
import fr.bloc_jo2024.entity.Utilisateur;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Telephone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelephone; // Renommé pour plus de clarté

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TelEnum typeTel;

    @Column(nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Numéro de téléphone invalide")
    private String numeroTelephone;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur"))
    private Utilisateur utilisateur;
}

enum TelEnum {
    MOBILE, FIXE;
}