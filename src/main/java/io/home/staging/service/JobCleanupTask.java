package io.home.staging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobCleanupTask {

    private final JobService jobService;

    /**
     * Cleans up all jobs that have been in PROCESSING state for too long.
     * Scheduled to run every hour on the hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupStaleJobs() {
        log.info("Starting scheduled stale job cleanup...");
        try {
            int cleanedCount = jobService.cleanupStaleJobs();
            log.info("Stale job cleanup completed. Cleaned {} jobs.", cleanedCount);
        } catch (Exception e) {
            log.error("Error occurred during stale job cleanup: ", e);
        }
    }
}
