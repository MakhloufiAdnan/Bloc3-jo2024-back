import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ParticiperId.class)
public class Participer {
    @Id
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    @JoinColumn(name = "idPays", nullable = false, foreignKey = @ForeignKey(name = "fk_participer_pays", foreignKeyDefinition = "FOREIGN KEY (idPays) REFERENCES Pays(idPays) ON DELETE CASCADE"))
    private Pays pays;

    @Id
    @ManyToOne
    @JoinColumn(name = "idEvenement", nullable = false)
    @JoinColumn(name = "idEvenement", nullable = false, foreignKey = @ForeignKey(name = "fk_participer_evenement", foreignKeyDefinition = "FOREIGN KEY (idEvenement) REFERENCES Evenements(idEvenement) ON DELETE CASCADE"))
    private Evenement evenement;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ParticiperId implements java.io.Serializable {
    private Long pays;
    private Long evenement;
}