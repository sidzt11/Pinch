package compression_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "compression.storage")
public class StorageConfig {
    
    /**
     * Directory path where temporary files will be stored during compression.
     */
    private String tempDir;
}
