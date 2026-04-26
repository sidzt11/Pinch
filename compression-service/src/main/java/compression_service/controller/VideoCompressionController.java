package compression_service.controller;

import compression_service.service.VideoCompressionService;
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
@RequestMapping("/api/v1/compress/video")
@RequiredArgsConstructor
public class VideoCompressionController {

    private final VideoCompressionService videoCompressionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> compressVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "crf", defaultValue = "28") String crf) {

        // Delegate to service layer
        Resource compressedVideo = videoCompressionService.compress(file, crf);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressed_video.mp4\"")
                .body(compressedVideo);
    }
}
