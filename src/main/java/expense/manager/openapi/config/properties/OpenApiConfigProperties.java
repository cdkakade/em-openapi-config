package expense.manager.openapi.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("openapi.config")
public class OpenApiConfigProperties {

	private String title;

	private String description;

	private String version;

	private Boolean errorResponse;

}
