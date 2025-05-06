package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.entity.enums.StatutOffre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OffreService {

    private static final String OFFER_NOT_FOUND = "Offre introuvable avec l'ID : ";

    private final OffreRepository offreRepository;

    public OffreService(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    public Offre getOffreById(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OFFER_NOT_FOUND + id));
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *") // Exécute cette méthode chaque minute
    public void mettreAJourStatutOffresExpirees() {
        List<Offre> offres = offreRepository.findByStatutOffre(StatutOffre.DISPONIBLE);
        LocalDateTime now = LocalDateTime.now();
        for (Offre offre : offres) {
            if (offre.getDateExpiration() != null && offre.getDateExpiration().isBefore(now)) {
                offre.setStatutOffre(StatutOffre.EXPIRE);
                offreRepository.save(offre);
            }
        }
    }
}