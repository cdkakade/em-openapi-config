package expense.manager.openapi.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("openapi.message")
public class OpenApiMessageProperties {

	private String badRequest;

	private String unAuthorized;

	private String forbidden;

	private String internalServerError;

	private String notFound;

	private String conflictRequest;

	private String unsupportedMediaType;

}
