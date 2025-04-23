package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "billets")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    @ManyToOne
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    // Relation Many-to-One vers l'entité Offre.
    @ManyToOne
    @JoinColumn(name = "id_offre", nullable = false)
    private Offre offre;
}