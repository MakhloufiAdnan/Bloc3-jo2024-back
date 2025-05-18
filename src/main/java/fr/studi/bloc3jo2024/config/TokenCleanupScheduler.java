package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;
import lombok.RequiredArgsConstructor; // Assure l'injection du service via le constructeur
import lombok.extern.slf4j.Slf4j; // Facilite la création d'un logger SLF4J
import org.springframework.scheduling.annotation.Scheduled; // Pour la planification de tâches
import org.springframework.stereotype.Component; // Indique que c'est un bean Spring

/**
 * Planificateur de tâches pour le nettoyage périodique des tokens expirés.
 * Utilise @Scheduled pour exécuter des tâches à intervalles réguliers ou à des moments précis.
 */
@Component
@RequiredArgsConstructor // Génère un constructeur avec les champs 'final' requis
@Slf4j // Injecte automatiquement un logger SLF4J (nommé 'log')
public class TokenCleanupScheduler {

    private final AuthTokenTemporaireService tokenService; // Service responsable de la gestion des tokens

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