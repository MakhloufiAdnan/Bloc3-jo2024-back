package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Telephone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelephoneRepository extends JpaRepository<Telephone, Long> {
    Telephone findByNumeroTelephone(String numeroTelephone);
}
