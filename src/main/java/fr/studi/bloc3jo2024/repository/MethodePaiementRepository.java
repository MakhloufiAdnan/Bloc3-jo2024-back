package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.MethodePaiement;
import fr.studi.bloc3jo2024.entity.enums.MethodePaiementEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MethodePaiementRepository extends JpaRepository<MethodePaiement, Long> {

    Optional<MethodePaiement> findByNomMethodePaiement(MethodePaiementEnum nomMethodePaiement);
}