package expense.manager.openapi.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiErrorRes {

	@Schema(required = true, example = "BAD_REQUEST")
	private HttpStatus status;

	@Schema(required = true, example = "1000")
	private int code;

	@Schema(required = true, example = "2022-11-03T12:49:03.864Z")
	private LocalDateTime timestamp;

	@Schema(required = true)
	private String message;

	private String debugMessage;

	private List<Object> errors;

}
