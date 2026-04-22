package compression_service.controller;

import compression_service.service.AudioCompressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/compress/audio")
@RequiredArgsConstructor
public class AudioCompressionController {

    private final AudioCompressionService audioCompressionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> compressAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bitrate", defaultValue = "128k") String bitrate) {

        // Outline implementation - delegates to service layer
        Resource compressedAudio = audioCompressionService.compress(file, bitrate);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressed_audio.mp3\"")
                .body(compressedAudio);
    }
}
