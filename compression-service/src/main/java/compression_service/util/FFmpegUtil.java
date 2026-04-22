package compression_service.util;

import java.nio.file.Path;

public interface FFmpegUtil {
    
    /**
     * Executes the FFmpeg command synchronously.
     * 
     * @param inputPath  Path to the input file
     * @param outputPath Path where the output should be saved
     * @param options    Additional FFmpeg flags and options
     * @return true if the process finished successfully, false otherwise
     */
    boolean compressMedia(Path inputPath, Path outputPath, String... options);
    
}
