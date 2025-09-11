package co.com.crediya.model.exception.specificExceptions;

import co.com.crediya.model.exception.DomainException;
import co.com.crediya.model.exception.ErrorKind;
import co.com.crediya.model.exception.UserErrorCode;

import java.util.List;

public final class NotFoundException extends DomainException {
    public NotFoundException(UserErrorCode code) {
        super(ErrorKind.NOT_FOUND, code.getCode(), code.getMessage(), List.of(code), null);
    }
}
