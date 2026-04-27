package compression_service.service;

import compression_service.exception.CompressionException;
import compression_service.util.FFmpegUtil;
import compression_service.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioCompressionServiceImpl implements AudioCompressionService {

    private final FileStorageUtil fileStorageUtil;
    private final FFmpegUtil ffmpegUtil;

    @Override
    public Resource compress(MultipartFile file, String bitrate) {
        log.info("Received request to compress audio file: {} to bitrate: {}", file.getOriginalFilename(), bitrate);
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty.");
        }
        
        Path inputPath = null;
        Path outputPath;

        try {
            // 1. Save uploaded file to temporary storage
            inputPath = fileStorageUtil.saveTempFile(file);

            // 2. Determine output path
            outputPath = fileStorageUtil.generateOutputFilePath(".mp3");

            // 3. Build and execute FFmpeg process
            // Example arguments for MP3 compression: -c:a libmp3lame -b:a 128k
            String[] options = {
                    "-c:a", "libmp3lame",
                    "-b:a", bitrate
            };

            boolean success = ffmpegUtil.compressMedia(inputPath, outputPath, options);

            if (!success) {
                // If compression failed, clean up the output file (if it was partially created)
                fileStorageUtil.deleteFile(outputPath);
                throw new CompressionException("FFmpeg compression failed. Check logs for details.");
            }

            // 4. Return the compressed file as a Resource
            // Note: The output file is left on disk to be streamed to the client.
            // A scheduled task (Phase 2) should clean up old output files.
            return new FileSystemResource(outputPath);

        } catch (IOException e) {
            log.error("Failed to handle file storage during compression", e);
            throw new CompressionException("File IO error during compression: " + e.getMessage(), e);
        } finally {
            // 5. Clean up temporary input file immediately
            if (inputPath != null) {
                fileStorageUtil.deleteFile(inputPath);
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Please upload a valid audio file. Received: " + contentType);
        }
    }
}
