package compression_service.scheduler;

import compression_service.config.StorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final StorageConfig storageConfig;

    /**
     * Runs every hour to clean up old files from the temporary storage directory.
     * Deletes files that are older than 1 hour.
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanUpOldFiles() {
        Path tempDir = Paths.get(storageConfig.getTempDir());
        if (!Files.exists(tempDir)) {
            return;
        }

        log.info("Running scheduled cleanup of temporary files in: {}", tempDir);
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        try (var stream = Files.list(tempDir)) {
            stream.filter(path -> {
                try {
                    return Files.isRegularFile(path) && Files.getLastModifiedTime(path).toInstant().isBefore(oneHourAgo);
                } catch (IOException e) {
                    log.warn("Could not read last modified time for file: {}", path, e);
                    return false;
                }
            }).forEach(path -> {
                try {
                    Files.delete(path);
                    log.info("Deleted old temporary file: {}", path);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary file during cleanup: {}", path, e);
                }
            });
        } catch (IOException e) {
            log.error("Error while listing files for cleanup in directory: {}", tempDir, e);
        }
    }
}
