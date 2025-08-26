package co.com.crediya.api.exception;

import co.com.crediya.api.ApiErrorType;
import jakarta.validation.ValidationException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);

        ApiErrorType errorType = determineErrorType(error);

        errorAttributes.put("status", errorType.code());
        errorAttributes.put("message", error.getMessage() != null ? error.getMessage() : errorType.message());
        errorAttributes.put("path", request.path());
        errorAttributes.put("error", errorType.message());

        return errorAttributes;
    }

    private ApiErrorType determineErrorType(Throwable error) {
        if (error instanceof IllegalArgumentException || error instanceof ValidationException) {
            return ApiErrorType.BAD_REQUEST;
        } else if (error instanceof ResourceNotFoundException) {
            return ApiErrorType.NOT_FOUND;
        } else if (error instanceof IllegalStateException) {
            return ApiErrorType.CONFLICT;
        } else {
            return ApiErrorType.INTERNAL_SERVER_ERROR;
        }
    }
}
