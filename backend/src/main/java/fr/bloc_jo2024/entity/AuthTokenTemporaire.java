import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.UUID;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthTokenTemporaire {
    @Id
    private UUID idTokenTemp;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    private AuthTokenTempEnum typeToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExpiration;

    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false)
    private Utilisateur utilisateur;
}

enum AuthTokenTempEnum {
    CONNEXION, RESET_PASSWORD;
}