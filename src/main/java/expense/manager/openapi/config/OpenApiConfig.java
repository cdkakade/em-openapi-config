package expense.manager.openapi.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import expense.manager.openapi.config.properties.OpenApiConfigProperties;
import expense.manager.openapi.config.properties.OpenApiMessageProperties;
import expense.manager.openapi.dto.ApiErrorRes;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Configuration
@PropertySource({ "classpath:openapi.properties" })
@ComponentScan("expense.manager.openapi.config.properties")
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class OpenApiConfig {

	private static final String BEARER_FORMAT = "JWT";

	private static final String SCHEME = "Bearer";

	private static final String SECURITY_SCHEME_NAME = "Security Scheme";

	private static final String COGNITO_USERNAME_HEADER = "vp-username";

	@Autowired
	private OpenApiConfigProperties configProperties;

	@Value("${spring.profiles.active:prod}")
	private String activeProfile;

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI().schemaRequirement(SECURITY_SCHEME_NAME, getSecurityScheme())
				.security(getSecurityRequirement()).info(info());
	}

	private Info info() {
		return new Info().title(configProperties.getTitle()).description(configProperties.getDescription())
				.version(configProperties.getVersion());
	}

	private List<SecurityRequirement> getSecurityRequirement() {
		SecurityRequirement securityRequirement = new SecurityRequirement();
		securityRequirement.addList(SECURITY_SCHEME_NAME);
		return List.of(securityRequirement);
	}

	private SecurityScheme getSecurityScheme() {
		SecurityScheme securityScheme = new SecurityScheme();
		if ("local".equalsIgnoreCase(activeProfile)) {
			securityScheme.type(SecurityScheme.Type.APIKEY);
			securityScheme.in(SecurityScheme.In.HEADER);
			securityScheme.setName(COGNITO_USERNAME_HEADER);
			securityScheme.description("Keycloak userid");
		}
		else {
			securityScheme.bearerFormat(BEARER_FORMAT);
			securityScheme.type(SecurityScheme.Type.HTTP);
			securityScheme.scheme(SCHEME);
		}
		return securityScheme;
	}

	@Bean
	public OpenApiCustomiser globalErrorResponseOpenApiCustomiser(
			@Autowired OpenApiMessageProperties messageProperties) {
		return openAPI -> {

			if (Boolean.TRUE.equals(configProperties.getErrorResponse())) {
				openAPI.getComponents().getSchemas().putAll(ModelConverters.getInstance().read(ApiErrorRes.class));
				Schema<ApiErrorRes> errorResponseSchema = new Schema<>();
				errorResponseSchema.setName("ApiErrorRes");
				errorResponseSchema.set$ref("#/components/schemas/ApiErrorRes");
				openAPI.getPaths().forEach((key, pathItem) -> pathItem.readOperations().forEach(operation -> {
					ApiResponses apiResponses = operation.getResponses();
					apiResponses.addApiResponse(String.valueOf(BAD_REQUEST.value()), createApiResponse(
							BAD_REQUEST.getReasonPhrase(), messageProperties.getBadRequest(), errorResponseSchema));
					apiResponses.addApiResponse(String.valueOf(UNAUTHORIZED.value()), createApiResponse(
							UNAUTHORIZED.getReasonPhrase(), messageProperties.getUnAuthorized(), errorResponseSchema));
					apiResponses.addApiResponse(String.valueOf(FORBIDDEN.value()), createApiResponse(
							FORBIDDEN.getReasonPhrase(), messageProperties.getForbidden(), errorResponseSchema));
					apiResponses.addApiResponse(String.valueOf(INTERNAL_SERVER_ERROR.value()),
							createApiResponse(INTERNAL_SERVER_ERROR.getReasonPhrase(),
									messageProperties.getInternalServerError(), errorResponseSchema));
					apiResponses.addApiResponse(String.valueOf(UNSUPPORTED_MEDIA_TYPE.value()),
							createApiResponse(UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
									messageProperties.getUnsupportedMediaType(), errorResponseSchema));
				}));
			}
		};
	}

	private ApiResponse createApiResponse(String description, String message, Schema<ApiErrorRes> schema) {
		ApiErrorRes apiErrorRes = new ApiErrorRes();
		apiErrorRes.setMessage(message);
		apiErrorRes.setCode(1000);
		apiErrorRes.setTimestamp(LocalDateTime.now());
		MediaType mediaType = new MediaType();
		mediaType.schema(schema);
		mediaType.example(apiErrorRes);
		return new ApiResponse().description(description).content(
				new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType));
	}

}
