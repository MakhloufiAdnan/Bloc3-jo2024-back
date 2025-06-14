package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Comporter;
import fr.studi.bloc3jo2024.entity.ComporterKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComporterRepository extends JpaRepository<Comporter, ComporterKey> {
}