package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Planificateur de tâches pour le nettoyage périodique des tokens expirés.
 * Utilise @Scheduled pour exécuter des tâches à intervalles réguliers ou à des moments précis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final AuthTokenTemporaireService tokenService;

    /**
     * Tâche planifiée pour purger les tokens temporaires expirés.
     * S'exécute tous les jours à minuit (00:00:00).
     * L'expression cron "0 0 0 * * ?" signifie :
     * - 0 : secondes
     * - 0 : minutes
     * - 0 : heures
     * - * : jour du mois (tous les jours)
     * - * : mois (tous les mois)
     * - ? : jour de la semaine (pas de jour spécifique, car jour du mois est spécifié)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeExpiredTokensJob() {
        log.info("Début de la tâche planifiée : purge des tokens temporaires expirés.");
        try {
            long count = tokenService.purgeExpiredTokens();
            log.info("Tâche de purge terminée. Nombre de tokens expirés supprimés : {}", count);
        } catch (Exception e) {
            log.error("Erreur lors de la purge des tokens expirés.", e);
        }
    }
}