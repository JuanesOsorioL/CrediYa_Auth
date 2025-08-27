package co.com.crediya.api.exception;

import co.com.crediya.usecase.user.exception.UserErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<UserErrorCode> errors;

    public ValidationException(List<UserErrorCode> errors) {
        super("Validation failed");
        this.errors = errors;
    }

}

