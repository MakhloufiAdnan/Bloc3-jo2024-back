package fr.studi.bloc3jo2024.repository;

import fr.studi.bloc3jo2024.entity.ContenuPanier;
import fr.studi.bloc3jo2024.entity.ContenuPanierId;
import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.Panier;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContenuPanierRepository extends JpaRepository<ContenuPanier, ContenuPanierId> {

    @Transactional
    void deleteByOffre(Offre offre);

    @Transactional
    void deleteByPanier(Panier panier);
}