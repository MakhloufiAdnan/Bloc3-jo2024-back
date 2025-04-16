package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_offre", nullable = false)
    private Offre offre;

    @Column(name = "cle_finale_billet", nullable = false, unique = true, columnDefinition = "TEXT")
    private String cleFinaleBillet;

    @Lob
    @Column(name = "qr_code_image")
    private byte[] qrCodeImage;

    @Column(name = "date_achat_billet", nullable = false)
    private LocalDateTime dateAchat;
}