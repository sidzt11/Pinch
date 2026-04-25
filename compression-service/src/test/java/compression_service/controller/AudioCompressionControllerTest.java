package compression_service.controller;

import compression_service.exception.CompressionException;
import compression_service.exception.GlobalExceptionHandler;
import compression_service.service.AudioCompressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AudioCompressionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AudioCompressionService audioCompressionService;

    @InjectMocks
    private AudioCompressionController audioCompressionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(audioCompressionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void compressAudio_ShouldReturn200AndResource_WhenSuccessful() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.wav", "audio/wav", "dummy content".getBytes());
        Resource dummyResource = new ByteArrayResource("compressed content".getBytes());
        String bitrate = "128k";

        when(audioCompressionService.compress(any(), eq(bitrate))).thenReturn(dummyResource);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/audio")
                        .file(file)
                        .param("bitrate", bitrate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("audio/mpeg")))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"compressed_audio.mp3\""));
    }

    @Test
    void compressAudio_ShouldReturn400_WhenFileIsEmpty() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.wav", "audio/wav", new byte[0]);

        when(audioCompressionService.compress(any(), any())).thenThrow(new IllegalArgumentException("Uploaded file cannot be empty."));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/audio")
                        .file(emptyFile)
                        .param("bitrate", "128k"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Uploaded file cannot be empty."));
    }

    @Test
    void compressAudio_ShouldReturn500_WhenCompressionFails() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.wav", "audio/wav", "dummy content".getBytes());

        when(audioCompressionService.compress(any(), any())).thenThrow(new CompressionException("FFmpeg compression failed"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/audio")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("FFmpeg compression failed"));
    }
}
