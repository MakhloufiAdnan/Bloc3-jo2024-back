package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.ContenuPanier;
import fr.bloc_jo2024.entity.ContenuPanierId;
import fr.bloc_jo2024.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContenuPanierRepository extends JpaRepository<ContenuPanier, ContenuPanierId> {

    @Transactional
    void deleteByOffre(Offre offre);

}