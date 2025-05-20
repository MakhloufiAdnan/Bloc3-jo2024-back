package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Planificateur pour le nettoyage périodique des tokens temporaires expirés.
 * Ce composant est activé ou désactivé via la propriété de configuration
 * 'app.scheduling.token-cleanup.enabled'.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.scheduling.token-cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Profile("!test")
public class TokenCleanupScheduler {

    private final AuthTokenTemporaireService tokenService;

    /**
     * Tâche planifiée pour purger les tokens temporaires expirés de la base de données.
     * S'exécute selon l'expression CRON définie dans la propriété 'application.scheduling.tokenCleanupCron'.
     * Par défaut, s'exécute tous les jours à minuit (00:00:00).
     * L'expression cron par défaut "0 0 0 * * ?" signifie :
     * - 0 : secondes
     * - 0 : minutes
     * - 0 : heures
     * - * : jour du mois (tous les jours)
     * - * : mois (tous les mois)
     * - ? : jour de la semaine (pas de jour spécifique, car 'jour du mois' est déjà spécifié avec '*')
     */
    @Scheduled(cron = "${application.scheduling.tokenCleanupCron:0 0 0 * * ?}")
    public void purgeExpiredTokensJob() {
        log.info("Début de la tâche planifiée : purge des tokens temporaires expirés.");
        try {
            // Appel au service pour effectuer la purge
            long count = tokenService.purgeExpiredTokens();
            log.info("Tâche de purge terminée. Nombre de tokens expirés supprimés : {}", count);
        } catch (Exception e) {
            log.error("Erreur lors de la purge des tokens expirés.", e);
        }
    }
}