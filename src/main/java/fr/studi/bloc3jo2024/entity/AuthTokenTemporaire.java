package fr.studi.bloc3jo2024.entity;

import fr.studi.bloc3jo2024.entity.enums.TypeAuthTokenTemp;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens_temporaire", indexes = {
        @Index(name = "idx_authtokentemp_token_identifier", columnList = "token_identifier", unique = true),
        @Index(name = "idx_authtokentemp_token_hache", columnList = "token_hache", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokenTemporaire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token_temp_uuid", columnDefinition = "UUID")
    private UUID idTokenTemp;

    @Column(name = "token_identifier", nullable = false, unique = true, length = 36)
    private String tokenIdentifier;

    @Column(name = "token_hache", nullable = false, unique = true , columnDefinition = "TEXT")
    private String tokenHache;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_token", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TypeAuthTokenTemp typeToken;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur_uuid", nullable = false)
    private Utilisateur utilisateur;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthTokenTemporaire that)) return false;
        return Objects.equals(idTokenTemp, that.idTokenTemp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTokenTemp);
    }

    @Override
    public String toString() {
        return "AuthTokenTemporaire{" +
                "idTokenTemp=" + idTokenTemp +
                ", tokenIdentifier='" + tokenIdentifier + '\'' +
                ", typeToken=" + typeToken +
                 ", dateExpiration=" + dateExpiration +
                ", isUsed=" + isUsed +
                (utilisateur != null ? ", utilisateurId=" + utilisateur.getIdUtilisateur() : "") +
                '}';
    }
}