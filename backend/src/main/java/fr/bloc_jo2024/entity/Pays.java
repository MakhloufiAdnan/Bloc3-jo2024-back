package fr.bloc_jo2024.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPays;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;
}
