package fr.studi.bloc3jo2024.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "billets", indexes = {
        @Index(name = "idx_billet_cle_finale", columnList = "cle_finale_billet", unique = true)
})
public class Billet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_billet")
    private Long idBillet;

    @Column(name = "cle_finale_billet", nullable = false, unique = true, columnDefinition = "TEXT")
    @ToString.Exclude
    private String cleFinaleBillet;

    @Lob
    @Column(name = "qr_code_image")
    @ToString.Exclude
    private byte[] qrCodeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false, foreignKey = @ForeignKey(name = "fk_billet_utilisateur"))
    @ToString.Exclude
    private Utilisateur utilisateur;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "billet_offre",
            joinColumns = @JoinColumn(name = "id_billet"),
            inverseJoinColumns = @JoinColumn(name = "id_offre")
    )
    @ToString.Exclude
    @Builder.Default
    private List<Offre> offres = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Billet that)) return false;
        if (this.idBillet == null || that.idBillet == null) {
            return false;
        }
        return Objects.equals(idBillet, that.idBillet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBillet);
    }

    @Override
    public String toString() {
        return "Billet{" +
                "idBillet=" + idBillet +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                ", nombreOffres=" + (offres != null ? offres.size() : 0) +
                '}';
    }
}