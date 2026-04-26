package compression_service.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface VideoCompressionService {
    
    /**
     * Compresses a video file using H.264 codec and a specific CRF (Constant Rate Factor).
     *
     * @param file The original video file received from the request
     * @param crf  The Constant Rate Factor for quality control (e.g., "28"). Lower is better quality, higher is higher compression.
     * @return The compressed video file as a Resource
     */
    Resource compress(MultipartFile file, String crf);
}
