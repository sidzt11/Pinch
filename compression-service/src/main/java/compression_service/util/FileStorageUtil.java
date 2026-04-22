package compression_service.util;

import compression_service.config.StorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageUtil {

    private final StorageConfig storageConfig;

    /**
     * Saves a MultipartFile to the configured temporary directory.
     * Generates a unique filename to prevent collisions.
     *
     * @param file The uploaded file
     * @return Path to the saved temporary file
     * @throws IOException If the file cannot be saved
     */
    public Path saveTempFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path targetLocation = getTempDir().resolve(uniqueFilename);

        log.debug("Saving temporary file to: {}", targetLocation);
        file.transferTo(targetLocation);
        
        return targetLocation;
    }

    /**
     * Generates a unique path for the output file in the temporary directory.
     *
     * @param extension The desired extension for the output file (e.g., ".mp3")
     * @return Path for the output file
     */
    public Path generateOutputFilePath(String extension) {
        String uniqueFilename = "out_" + UUID.randomUUID().toString() + (extension.startsWith(".") ? extension : "." + extension);
        return getTempDir().resolve(uniqueFilename);
    }

    /**
     * Deletes a file if it exists. Useful for cleanup in finally blocks.
     *
     * @param path The path of the file to delete
     */
    public void deleteFile(Path path) {
        if (path == null) return;
        
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.debug("Deleted temporary file: {}", path);
            }
        } catch (IOException e) {
            log.warn("Failed to delete temporary file: {}", path, e);
        }
    }

    /**
     * Ensures the temporary directory exists and returns its Path.
     */
    private Path getTempDir() {
        Path tempPath = Paths.get(storageConfig.getTempDir());
        try {
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
                log.info("Created temporary storage directory at: {}", tempPath);
            }
            return tempPath;
        } catch (IOException e) {
            log.error("Could not initialize temporary storage directory at: {}", tempPath, e);
            throw new RuntimeException("Failed to initialize storage directory", e);
        }
    }

    /**
     * Extracts the extension from a filename.
     */
    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
