import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SelectionnerId.class)
public class Selectionner {
    @Id
    @ManyToOne
    @JoinColumn(name = "idUtilisateur", nullable = false)
    @JoinColumn(name = "idUtilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_utilisateur_selectionner", foreignKeyDefinition = "FOREIGN KEY (idUtilisateur) REFERENCES Utilisateurs(idUtilisateur) ON DELETE CASCADE"))
    private Utilisateur utilisateur;

    @Id
    @ManyToOne
    @JoinColumn(name = "idOffre", nullable = false)
    @JoinColumn(name = "idOffre", nullable = false, foreignKey = @ForeignKey(name = "fk_offre_selectionner", foreignKeyDefinition = "FOREIGN KEY (idOffre) REFERENCES Offres(idOffre) ON DELETE CASCADE"))
    private Offre offre;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SelectionnerId implements java.io.Serializable {
    private Long utilisateur;
    private Long offre;
}
