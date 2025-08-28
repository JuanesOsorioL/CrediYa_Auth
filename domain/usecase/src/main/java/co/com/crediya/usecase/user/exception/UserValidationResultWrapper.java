package co.com.crediya.usecase.user.exception;

import co.com.crediya.model.user.User;
import lombok.Getter;

import java.util.List;

@Getter
public class UserValidationResultWrapper {
    private final User user;
    private final List<UserErrorCode> infraErrors;
    private final List<UserErrorCode> domainErrors;

    public UserValidationResultWrapper(User user, List<UserErrorCode> infraErrors, List<UserErrorCode> domainErrors) {
        this.user = user;
        this.infraErrors = infraErrors;
        this.domainErrors = domainErrors;
    }
}
