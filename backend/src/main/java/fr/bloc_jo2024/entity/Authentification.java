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
public class Authentification {
    @Id
    private UUID idToken;

    @Column(nullable = false, unique = true)
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExpiration;

    @Column(nullable = false)
    private String motPasse;

    @Column(nullable = false)
    private String salt;

    @OneToOne
    @JoinColumn(name = "idUtilisateur", nullable = false, unique = true, onDelete = ForeignKeyAction.CASCADE)
    private Utilisateur utilisateur;
}