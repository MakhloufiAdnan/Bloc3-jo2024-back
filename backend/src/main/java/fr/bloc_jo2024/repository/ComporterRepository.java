package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Comporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComporterRepository extends JpaRepository<Comporter, Integer> {
}
