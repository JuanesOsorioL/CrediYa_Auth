package co.com.crediya.model.exception;

import java.util.List;

public abstract class DomainException extends RuntimeException {
    private final ErrorKind kind;
    private final String code;
    private final List<UserErrorCode> errors;

    protected DomainException(ErrorKind kind, String code, String message, List<UserErrorCode> errors, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.code = code;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public ErrorKind kind() { return kind; }
    public String code() { return code; }
    public List<UserErrorCode> errors() { return errors; }
}
