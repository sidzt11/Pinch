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
public class VideoCompressionServiceImpl implements VideoCompressionService {

    private final FileStorageUtil fileStorageUtil;
    private final FFmpegUtil ffmpegUtil;

    @Override
    public Resource compress(MultipartFile file, String crf) {
        log.info("Received request to compress video file: {} with CRF: {}", file.getOriginalFilename(), crf);
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty.");
        }
        
        Path inputPath = null;
        Path outputPath = null;

        try {
            // 1. Save uploaded file to temporary storage
            inputPath = fileStorageUtil.saveTempFile(file);

            // 2. Determine output path
            outputPath = fileStorageUtil.generateOutputFilePath(".mp4");

            // 3. Build and execute FFmpeg process for video
            // Arguments for H.264 compression:
            // -c:v libx264 : Video codec H.264
            // -crf : Constant Rate Factor for quality (default typically 23, but we'll accept parameter)
            // -preset fast : Encoding speed/compression ratio tradeoff
            // -c:a aac -b:a 128k : Audio re-encoding to AAC at 128kbps for broad compatibility
            String[] options = {
                    "-c:v", "libx264",
                    "-crf", crf,
                    "-preset", "fast", // Good balance between speed and compression
                    "-c:a", "aac",
                    "-b:a", "128k"
            };

            boolean success = ffmpegUtil.compressMedia(inputPath, outputPath, options);

            if (!success) {
                // If compression failed, clean up the output file
                fileStorageUtil.deleteFile(outputPath);
                throw new CompressionException("FFmpeg video compression failed. Check logs for details.");
            }

            // 4. Return the compressed file as a Resource
            return new FileSystemResource(outputPath);

        } catch (IOException e) {
            log.error("Failed to handle file storage during video compression", e);
            throw new CompressionException("File IO error during video compression: " + e.getMessage(), e);
        } finally {
            // 5. Clean up temporary input file immediately
            if (inputPath != null) {
                fileStorageUtil.deleteFile(inputPath);
            }
        }
    }
}
