package compression_service.controller;

import compression_service.exception.CompressionException;
import compression_service.exception.GlobalExceptionHandler;
import compression_service.service.VideoCompressionService;
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
class VideoCompressionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoCompressionService videoCompressionService;

    @InjectMocks
    private VideoCompressionController videoCompressionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(videoCompressionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void compressVideo_ShouldReturn200AndResource_WhenSuccessful() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.mov", "video/quicktime", "dummy content".getBytes());
        Resource dummyResource = new ByteArrayResource("compressed content".getBytes());
        String crf = "28";

        when(videoCompressionService.compress(any(), eq(crf))).thenReturn(dummyResource);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/video")
                        .file(file)
                        .param("crf", crf))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("video/mp4")))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"compressed_video.mp4\""));
    }

    @Test
    void compressVideo_ShouldReturn400_WhenFileIsEmpty() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.mp4", "video/mp4", new byte[0]);

        when(videoCompressionService.compress(any(), any())).thenThrow(new IllegalArgumentException("Uploaded file cannot be empty."));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/video")
                        .file(emptyFile)
                        .param("crf", "28"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Uploaded file cannot be empty."));
    }

    @Test
    void compressVideo_ShouldReturn500_WhenCompressionFails() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.mkv", "video/x-matroska", "dummy content".getBytes());

        when(videoCompressionService.compress(any(), any())).thenThrow(new CompressionException("FFmpeg video compression failed"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/compress/video")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("FFmpeg video compression failed"));
    }
}
