package co.com.crediya.model.exception.specificExceptions;

import co.com.crediya.model.exception.DomainException;
import co.com.crediya.model.exception.ErrorKind;
import co.com.crediya.model.exception.UserErrorCode;

import java.util.List;

public final class TechnicalException extends DomainException {
    public TechnicalException(String message, Throwable cause) {
        super(ErrorKind.TECHNICAL, UserErrorCode.GENERIC_ERROR.getCode(), message, List.of(UserErrorCode.GENERIC_ERROR), cause);
    }
}
