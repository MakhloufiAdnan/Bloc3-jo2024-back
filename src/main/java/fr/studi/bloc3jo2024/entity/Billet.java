package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "billets")
@Getter
@Setter
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
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "qr_code_image")
    private byte[] qrCodeImage;

    @Column(name = "is_scanned", nullable = false)
    private boolean isScanned = false;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "billet_offre",
            joinColumns = @JoinColumn(name = "id_billet"),
            inverseJoinColumns = @JoinColumn(name = "id_offre")
    )
    private List<Offre> offres;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Billet billet)) return false;
        return Objects.equals(idBillet, billet.idBillet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBillet);
    }

    @Override
    public String toString() {
        return "Billet{" +
                "idBillet=" + idBillet +
                ", isScanned=" + isScanned +
                ", scannedAt=" + scannedAt +
                ", purchaseDate=" + purchaseDate +
                ", cleFinaleBillet='" + (cleFinaleBillet != null ? "[PRESENT]" : "null") + '\'' +
                ", qrCodeImage=" + (qrCodeImage != null ? "[PRESENT]" : "null") +
                ", utilisateurId=" + (utilisateur != null && utilisateur.getIdUtilisateur() != null ? utilisateur.getIdUtilisateur() : "null") +
                ", offresCount=" + (offres != null ? offres.size() : 0) +
                '}';
    }
}