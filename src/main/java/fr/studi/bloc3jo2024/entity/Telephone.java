package fr.studi.bloc3jo2024.entity;


import fr.studi.bloc3jo2024.entity.enums.TypeTel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "utilisateur")
@Table(name = "telephones")
public class Telephone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telephone")
    private Long idTelephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_tel",nullable = false)
    private TypeTel typeTel;

    // Numéro de téléphone, doit être unique et accepté avec l'indicatif international.
    @Column(name = "numero_telephone", nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^(\\+\\d{1,3}[ -]?)?\\(?\\d{1,4}\\)?[ -]?\\d{1,4}[ -]?\\d{1,4}[ -]?\\d{1,4}$", message = "Numéro de téléphone invalide")
    private String numeroTelephone;

    // Relation Many-to-One vers l'entité Utilisateur. Chaque numéro de téléphone appartient à un utilisateur.
    @ManyToOne
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;
}