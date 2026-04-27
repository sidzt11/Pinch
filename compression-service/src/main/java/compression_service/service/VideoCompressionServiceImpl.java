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
public class VideoCompressionServiceImpl implements VideoCompressionService {

    private final FileStorageUtil fileStorageUtil;
    private final FFmpegUtil ffmpegUtil;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-matroska",
            "video/webm", "video/x-flv", "video/x-ms-wmv"
    );

    @Override
    public Resource compress(MultipartFile file, String crf) {
        log.info("Received request to compress video file: {} with CRF: {}", file.getOriginalFilename(), crf);
        
        validateFile(file);
        
        Path inputPath = null;
        Path outputPath;

        try {
            // 1. Save uploaded file to temporary storage
            inputPath = fileStorageUtil.saveTempFile(file);

            // 2. Determine output path
            outputPath = fileStorageUtil.generateOutputFilePath(".mp4");

            // 3. Build and execute FFmpeg process for video
            String[] options = {
                    "-c:v", "libx264",
                    "-crf", crf,
                    "-preset", "fast",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-threads", "0"
            };

            boolean success = ffmpegUtil.compressMedia(inputPath, outputPath, options);

            if (!success) {
                fileStorageUtil.deleteFile(outputPath);
                throw new CompressionException("FFmpeg video compression failed. Check logs for details.");
            }

            return new FileSystemResource(outputPath);

        } catch (IOException e) {
            log.error("Failed to handle file storage during video compression", e);
            throw new CompressionException("File IO error during video compression: " + e.getMessage(), e);
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
            throw new IllegalArgumentException("Invalid file type. Please upload a valid video file. Received: " + contentType);
        }
    }
}
