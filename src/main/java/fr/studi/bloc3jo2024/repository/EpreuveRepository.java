package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Epreuve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpreuveRepository extends JpaRepository<Epreuve, Long> {

    /**
     * Trouve toutes les épreuves marquées comme "en vedette" SANS pagination,
     * en chargeant les entités Comporter et Discipline associées.
     * @return Une liste d'épreuves en vedette avec leurs détails.
     */
    @Query("SELECT DISTINCT e FROM Epreuve e " +
            "LEFT JOIN FETCH e.comporte c " +
            "LEFT JOIN FETCH c.discipline d " +
            "WHERE e.isFeatured = true")
    List<Epreuve> findByIsFeaturedTrueWithComportersAndDisciplines();

    /**
     * Trouve toutes les épreuves marquées comme "en vedette" AVEC pagination,
     * en chargeant les entités Comporter et Discipline associées.
     * @param pageable L'objet de pagination.
     * @return Une page d'épreuves en vedette avec leurs détails.
     */
    @Query(value = "SELECT DISTINCT e FROM Epreuve e " +
            "LEFT JOIN FETCH e.comporte c " +
            "LEFT JOIN FETCH c.discipline d " +
            "WHERE e.isFeatured = true",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Epreuve e WHERE e.isFeatured = true")
    Page<Epreuve> findByIsFeaturedTrueWithComportersAndDisciplines(Pageable pageable);

    /**
     * Récupère toutes les épreuves avec pagination,
     * en chargeant les entités Comporter et Discipline associées.
     * @param pageable L'objet de pagination.
     * @return Une page d'épreuves avec leurs détails.
     */
    @Query(value = "SELECT DISTINCT e FROM Epreuve e " +
            "LEFT JOIN FETCH e.comporte c " +
            "LEFT JOIN FETCH c.discipline d",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Epreuve e")
    Page<Epreuve> findAllWithComportersAndDisciplines(Pageable pageable);

    /**
     * Trouve une épreuve par son ID,
     * en chargeant les entités Comporter et Discipline associées.
     * @param id L'ID de l'épreuve.
     * @return Un Optional contenant l'épreuve avec ses détails si trouvée.
     */
    @Query("SELECT DISTINCT e FROM Epreuve e " +
            "LEFT JOIN FETCH e.comporte c " +
            "LEFT JOIN FETCH c.discipline d " +
            "WHERE e.idEpreuve = :id")
    Optional<Epreuve> findByIdWithComportersAndDisciplines(@Param("id") Long id);


    /**
     * Trouve toutes les épreuves marquées comme "en vedette".
     * (Version simple sans fetch explicite des relations autres que celles par défaut)
     * @return Une liste d'épreuves en vedette.
     */
    List<Epreuve> findByIsFeaturedTrue();

    /**
     * Récupère toutes les épreuves avec pagination.
     * (Version simple de JpaRepository sans fetch explicite des relations autres que celles par défaut)
     * @param pageable L'objet de pagination.
     * @return Une page d'épreuves.
     */
    @Override
    @NonNull
    Page<Epreuve> findAll(@NonNull Pageable pageable);
}