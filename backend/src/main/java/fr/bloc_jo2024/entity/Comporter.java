package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ComporterId.class)
public class Comporter {
    @Id
    @ManyToOne
    @JoinColumn(name = "idEpreuve", nullable = false)
    private Epreuve epreuve;

    @Id
    @ManyToOne
    @JoinColumn(name = "idEvenement", nullable = false, referencedColumnName = "idEvenement")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evenement evenement;

    @Column(nullable = false)
    private boolean jrDeMedaille;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ComporterId implements java.io.Serializable {
    private Long epreuve;
    private Long evenement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComporterId)) return false;
        ComporterId that = (ComporterId) o;
        return Objects.equals(epreuve, that.epreuve) && Objects.equals(evenement, that.evenement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epreuve, evenement);
    }
}

