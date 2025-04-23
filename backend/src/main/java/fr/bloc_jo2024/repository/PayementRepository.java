package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Payement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayementRepository extends CrudRepository<Payement, Long> {
}
