package io.home.staging.service;

import io.home.staging.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {

    private final TokenRepository tokenRepository;

    /**
     * Cleans up all expired or revoked tokens from the database.
     * Scheduled to run every hour on the hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled token cleanup...");
        try {
            tokenRepository.deleteExpiredOrRevokedTokens();
            log.info("Token cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during token cleanup: ", e);
        }
    }
}
