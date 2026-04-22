package compression_service.util;

import compression_service.config.FFmpegConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FFmpegUtilImpl implements FFmpegUtil {

    private final FFmpegConfig ffmpegConfig;

    @Override
    public boolean compressMedia(Path inputPath, Path outputPath, String... options) {
        
        List<String> command = new ArrayList<>();
        command.add(ffmpegConfig.getPath());
        command.add("-y"); // Overwrite output files without asking
        command.add("-i");
        command.add(inputPath.toAbsolutePath().toString());
        
        // Add specific compression options
        if (options != null && options.length > 0) {
            command.addAll(Arrays.asList(options));
        }

        command.add(outputPath.toAbsolutePath().toString());

        log.info("Executing FFmpeg command: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Merge stderr into stdout to capture all FFmpeg logs

        try {
            Process process = processBuilder.start();

            // Read the output stream to prevent the process from hanging and to log FFmpeg output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line); // Log at debug level to avoid spamming info, but keep it available
                }
            }

            // Wait for the process to complete with a timeout (e.g., 5 minutes)
            // This is crucial for Phase 1 (synchronous) to avoid hanging threads indefinitely
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (!finished) {
                log.error("FFmpeg process timed out after 5 minutes.");
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();
            
            if (exitCode != 0) {
                log.error("FFmpeg process exited with non-zero code: {}", exitCode);
                return false;
            }

            log.info("FFmpeg compression completed successfully.");
            return true;

        } catch (IOException | InterruptedException e) {
            log.error("Exception occurred while executing FFmpeg process: ", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
            return false;
        }
    }
}
