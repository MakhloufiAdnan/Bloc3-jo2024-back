package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.Billet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BilletRepository extends JpaRepository<Billet, Long> {
}
