package co.com.crediya.api.exception;

import co.com.crediya.api.dto.ApiRespons;
import co.com.crediya.usecase.user.exception.DomainValidationException;
import co.com.crediya.usecase.user.exception.UserErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InfrastructureValidationException.class, DomainValidationException.class})
    public Mono<ServerResponse> handleValidationExceptions(RuntimeException ex) {
        List<UserErrorCode> infraErrors = List.of();
        List<UserErrorCode> domainErrors = List.of();

        if (ex instanceof InfrastructureValidationException) {
            infraErrors = ((InfrastructureValidationException) ex).getInfraErrors();
        }
        if (ex instanceof DomainValidationException) {
            domainErrors = ((DomainValidationException) ex).getDomainErrors();
        }

        List<UserErrorCode> allErrors = Stream.concat(infraErrors.stream(), domainErrors.stream())
                .distinct()
                .toList();

        ApiRespons<List<String>> errorResponse = new ApiRespons<>();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Errores de validación");
        errorResponse.setBody(allErrors.stream().map(UserErrorCode::getMessage).toList());

        return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> handleGenericException(Exception ex) {
        ApiRespons<List<String>> errorResponse = new ApiRespons<>();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage("Error interno del servidor");
        errorResponse.setBody(List.of(UserErrorCode.GENERIC_ERROR.getMessage()));
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(errorResponse);
    }
}