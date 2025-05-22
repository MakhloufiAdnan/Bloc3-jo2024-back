package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "billets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_billet")
    private Long idBillet;

    @Column(name = "cle_finale_billet", nullable = false, unique = true, columnDefinition = "TEXT")
    private String cleFinaleBillet;

    @Lob
    @Column(name = "qr_code_image")
    private byte[] qrCodeImage;

    // Relation Many-to-One vers l'entité Utilisateur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    // Relation Many-to-Many vers l'entité Offre.
    @ManyToMany
    @JoinTable(
            name = "billet_offre",
            joinColumns = @JoinColumn(name = "id_billet"),
            inverseJoinColumns = @JoinColumn(name = "id_offre")
    )
    @EqualsAndHashCode.Exclude
    private List<Offre> offres;
}