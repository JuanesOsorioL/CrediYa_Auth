package co.com.crediya.model.exception.specific_exceptions;

import co.com.crediya.model.exception.DomainException;
import co.com.crediya.model.exception.ErrorKind;
import co.com.crediya.model.exception.UserErrorCode;

import java.util.List;

public final class BadRequestException extends DomainException {
    public BadRequestException(UserErrorCode code) {
        super(ErrorKind.BAD_REQUEST, code.getCode(), code.getMessage(), List.of(code), null);
    }
}
