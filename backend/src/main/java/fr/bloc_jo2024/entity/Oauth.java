import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Oauth {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idOAuth;

    @Column(unique = true)
    private String googleId;

    @Column(unique = true)
    private String facebookId;

    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Utilisateur utilisateur;
}