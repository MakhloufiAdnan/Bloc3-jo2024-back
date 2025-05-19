package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OffreService {

    private static final Logger log = LoggerFactory.getLogger(OffreService.class);
    private static final String OFFRE_NOT_FOUND_ID_PREFIX = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;

    /**
     * Récupère une offre par son ID.
     * @param id L'ID de l'offre.
     * @return L'entité Offre.
     * @throws ResourceNotFoundException si l'offre n'est pas trouvée.
     */
    @Transactional(readOnly = true)
    public Offre getOffreById(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OFFRE_NOT_FOUND_ID_PREFIX + id));
    }

    /**
     * Tâche planifiée pour mettre à jour le statut des offres expirées.
     * S'exécute toutes les minutes par défaut (configurable via application.properties).
     * Utilise une requête de mise à jour en masse pour l'efficacité.
     */
    @Transactional
    @Scheduled(cron = "${application.scheduling.updateExpiredOffersCron:0 * * * * ?}")
    public void mettreAJourStatutOffresExpirees() {
        LocalDateTime maintenant = LocalDateTime.now();
        log.info("Exécution de la tâche de mise à jour des offres expirées à {}", maintenant);
        int updatedCount = offreRepository.updateStatusForExpiredOffers(maintenant);
        if (updatedCount > 0) {
            log.info("{} offres ont été mises à jour comme EXPIREES.", updatedCount);
        } else {
            log.info("Aucune offre à mettre à jour comme EXPIREE.");
        }
    }
}