package fr.studi.bloc3jo2024.service.offres;

import fr.studi.bloc3jo2024.entity.Offre;
import fr.studi.bloc3jo2024.exception.ResourceNotFoundException;
import fr.studi.bloc3jo2024.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OffreService {

    private static final Logger log = LoggerFactory.getLogger(OffreService.class);
    private static final String OFFRE_NOT_FOUND_ID_PREFIX = "Offre non trouvée avec l'ID : ";

    private final OffreRepository offreRepository;

    @Transactional(readOnly = true)
    public Offre getOffreById(Long id) {
        // ... (inchangé)
        log.debug("Tentative de récupération de l'offre avec ID : {}", id);
        return offreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Aucune offre trouvée avec l'ID : {}", id);
                    return new ResourceNotFoundException(OFFRE_NOT_FOUND_ID_PREFIX + id);
                });
    }

    @Transactional
    @Scheduled(cron = "${application.scheduling.updateExpiredOffersCron:0 0 1 * * *}") // Tous les jours à 1h
    public void mettreAJourStatutOffresExpireesAutomatiquement() {
        LocalDateTime maintenantDateTime = LocalDateTime.now();
        LocalDate maintenantDate = LocalDate.now();
        log.info("Exécution de la tâche planifiée de mise à jour des offres (basée sur date offre et date discipline) à {}", maintenantDateTime);
        try {
            int updatedCount = offreRepository.updateStatusForEffectivelyExpiredOffers(maintenantDateTime, maintenantDate);
            if (updatedCount > 0) {
                log.info("{} offres ont été mises à jour avec le statut EXPIRE.", updatedCount);
            } else {
                log.info("Aucune offre à mettre à jour comme EXPIREE lors de cette exécution.");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour automatique du statut des offres expirées", e);
        }
    }
}