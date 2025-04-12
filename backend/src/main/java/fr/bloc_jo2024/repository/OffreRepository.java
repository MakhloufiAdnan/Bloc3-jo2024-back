package fr.bloc_jo2024.repository;

import fr.bloc_jo2024.entity.Offre;
import fr.bloc_jo2024.entity.enums.TypeOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    // Recherche des offres par type
    List<Offre> findByTypeOffre(TypeOffre typeOffre);

    // Recherche des offres par QR code (uniquement si nécessaire)
    Offre findByQrCode(String qrCode);

    // Recherche des offres par statut
    List<Offre> findByStatutOffre(String statutOffre);
}
