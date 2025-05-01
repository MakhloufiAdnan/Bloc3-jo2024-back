package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaysRepository extends JpaRepository<Pays, Long> {

    Optional<Pays> findByNomPays(String nom);
}
