package co.com.crediya.usecase.user.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DomainValidationException extends RuntimeException {
    private final List<UserErrorCode> errors;

    public DomainValidationException(List<UserErrorCode> errors) {
        super("Errores de validación en dominio");
        this.errors = errors;
    }

}
