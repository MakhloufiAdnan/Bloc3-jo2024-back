package fr.studi.bloc3jo2024.config;

import fr.studi.bloc3jo2024.service.AuthTokenTemporaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final AuthTokenTemporaireService tokenService;

    // Tous les jours à minuit
    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeExpiredTokensJob() {
        long count = tokenService.purgeExpiredTokens();
        log.info("Tokens expirés purgés : {}", count);
    }
}
