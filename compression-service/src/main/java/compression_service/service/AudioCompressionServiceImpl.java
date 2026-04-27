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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioCompressionServiceImpl implements AudioCompressionService {

    private final FileStorageUtil fileStorageUtil;
    private final FFmpegUtil ffmpegUtil;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "audio/mpeg", "audio/wav", "audio/x-wav", "audio/wave", "audio/vnd.wave",
            "audio/aac", "audio/mp4", "audio/flac", "audio/ogg", "audio/vorbis"
    );

    @Override
    public Resource compress(MultipartFile file, String bitrate) {
        log.info("Received request to compress audio file: {} to bitrate: {}", file.getOriginalFilename(), bitrate);
        
        validateFile(file);
        
        Path inputPath = null;
        Path outputPath;

        try {
            // 1. Save uploaded file to temporary storage
            inputPath = fileStorageUtil.saveTempFile(file);

            // 2. Determine output path
            outputPath = fileStorageUtil.generateOutputFilePath(".mp3");

            // 3. Build and execute FFmpeg process
            String[] options = {
                    "-c:a", "libmp3lame",
                    "-b:a", bitrate,
                    "-threads", "0"
            };

            boolean success = ffmpegUtil.compressMedia(inputPath, outputPath, options);

            if (!success) {
                fileStorageUtil.deleteFile(outputPath);
                throw new CompressionException("FFmpeg compression failed. Check logs for details.");
            }

            return new FileSystemResource(outputPath);

        } catch (IOException e) {
            log.error("Failed to handle file storage during compression", e);
            throw new CompressionException("File IO error during compression: " + e.getMessage(), e);
        } finally {
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
