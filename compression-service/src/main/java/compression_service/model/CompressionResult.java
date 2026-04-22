package compression_service.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class CompressionResult {
    private boolean success;
    private String errorMessage;
    private Resource outputResource;
}
