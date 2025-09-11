package co.com.crediya.usecase.exception;

import co.com.crediya.model.exception.DomainException;
import co.com.crediya.model.exception.ErrorKind;
import co.com.crediya.model.exception.UserErrorCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

@Getter
public class UserValidationException extends DomainException {
    private final List<UserErrorCode> infraErrors;
    private final List<UserErrorCode> domainErrors;

    public UserValidationException(List<UserErrorCode> infraErrors, List<UserErrorCode> domainErrors) {
        super(ErrorKind.VALIDATION, null, "Errores de validación de usuario",
                Stream.concat(infraErrors.stream(), domainErrors.stream()).distinct().toList(),
                null);
        this.infraErrors = infraErrors;
        this.domainErrors = domainErrors;
    }

    public List<UserErrorCode> all() {
        return Stream.concat(infraErrors.stream(), domainErrors.stream()).distinct().toList();
    }
}



//@Getter
//public class UserValidationException extends RuntimeException {
//    private final List<UserErrorCode> infraErrors;
//    private final List<UserErrorCode> domainErrors;
//
//    public UserValidationException(List<UserErrorCode> infraErrors, List<UserErrorCode> domainErrors) {
//        super("Errores de validación de usuario");
//        this.infraErrors = infraErrors;
//        this.domainErrors = domainErrors;
//    }
//}
