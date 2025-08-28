package co.com.crediya.api.exception;

import co.com.crediya.usecase.user.exception.UserErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public class InfrastructureValidationException extends RuntimeException {
    private final List<UserErrorCode> infraErrors;

    public InfrastructureValidationException(List<UserErrorCode> errors) {
        super("Validation failed");
        this.infraErrors = errors;
    }

}

