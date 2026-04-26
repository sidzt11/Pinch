package compression_service.service;

import compression_service.exception.CompressionException;
import compression_service.util.FFmpegUtil;
import compression_service.util.FileStorageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoCompressionServiceImplTest {

    @Mock
    private FileStorageUtil fileStorageUtil;

    @Mock
    private FFmpegUtil ffmpegUtil;

    @InjectMocks
    private VideoCompressionServiceImpl videoCompressionService;

    @Test
    void compress_ShouldReturnResource_WhenCompressionSucceeds() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.mov", "video/quicktime", "dummy content".getBytes());
        String crf = "28";
        Path inputPath = Paths.get("/tmp/input.mov");
        Path outputPath = Paths.get("/tmp/output.mp4");

        when(fileStorageUtil.saveTempFile(file)).thenReturn(inputPath);
        when(fileStorageUtil.generateOutputFilePath(".mp4")).thenReturn(outputPath);
        
        // Mockito's any() when used with varargs in Java sometimes needs to be matched differently.
        // We can just use any(String[].class) for the varargs parameter.
        when(ffmpegUtil.compressMedia(eq(inputPath), eq(outputPath), any(String[].class))).thenReturn(true);

        // Act
        Resource result = videoCompressionService.compress(file, crf);

        // Assert
        assertNotNull(result);
        assertEquals(outputPath.toFile().getName(), result.getFilename());
        verify(fileStorageUtil).deleteFile(inputPath); // verify input file is cleaned up
    }

    @Test
    void compress_ShouldThrowIllegalArgumentException_WhenFileIsEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.mp4", "video/mp4", new byte[0]);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoCompressionService.compress(emptyFile, "28");
        });
        assertEquals("Uploaded file cannot be empty.", exception.getMessage());
        verifyNoInteractions(fileStorageUtil, ffmpegUtil);
    }

    @Test
    void compress_ShouldThrowCompressionException_WhenFFmpegFails() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.mkv", "video/x-matroska", "dummy content".getBytes());
        Path inputPath = Paths.get("/tmp/input.mkv");
        Path outputPath = Paths.get("/tmp/output.mp4");

        when(fileStorageUtil.saveTempFile(file)).thenReturn(inputPath);
        when(fileStorageUtil.generateOutputFilePath(".mp4")).thenReturn(outputPath);
        when(ffmpegUtil.compressMedia(eq(inputPath), eq(outputPath), any(String[].class))).thenReturn(false);

        // Act & Assert
        CompressionException exception = assertThrows(CompressionException.class, () -> {
            videoCompressionService.compress(file, "28");
        });
        assertTrue(exception.getMessage().contains("FFmpeg video compression failed"));
        
        // Verify cleanup
        verify(fileStorageUtil).deleteFile(outputPath); // Output should be cleaned up on failure
        verify(fileStorageUtil).deleteFile(inputPath);  // Input should always be cleaned up
    }
}
