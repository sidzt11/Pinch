package compression_service.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AudioCompressionService {
    
    /**
     * Compresses an audio file to the specified bitrate.
     *
     * @param file    The original audio file received from the request
     * @param bitrate The target bitrate for the compression (e.g., "128k")
     * @return The compressed audio file as a Resource
     */
    Resource compress(MultipartFile file, String bitrate);
}
