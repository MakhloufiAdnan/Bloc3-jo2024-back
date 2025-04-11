package fr.bloc_jo2024.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ParticiperId.class)
public class Participer {
    @Id
    @ManyToOne
    @JoinColumn(name = "idPays", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pays pays;

    @Id
    @ManyToOne
    @JoinColumn(name = "idEvenement", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evenement evenement;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ParticiperId implements Serializable {
    private Long pays;
    private Long evenement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticiperId)) return false;
        ParticiperId that = (ParticiperId) o;
        return pays.equals(that.pays) && evenement.equals(that.evenement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pays, evenement);
    }
}
