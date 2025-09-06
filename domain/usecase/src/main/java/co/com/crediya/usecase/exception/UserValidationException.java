package co.com.crediya.usecase.exception;

import co.com.crediya.model.exception.UserErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public class UserValidationException extends RuntimeException {
    private final List<UserErrorCode> infraErrors;
    private final List<UserErrorCode> domainErrors;

    public UserValidationException(List<UserErrorCode> infraErrors, List<UserErrorCode> domainErrors) {
        super("Errores de validación de usuario");
        this.infraErrors = infraErrors;
        this.domainErrors = domainErrors;
    }
}
