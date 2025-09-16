package co.com.crediya.usecase.exception;

import co.com.crediya.model.exception.ErrorKind;
import co.com.crediya.model.exception.UserErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationExceptionTest {

    @Test
    void all_returnsDistinctUnionPreservingOrder() {
        List<UserErrorCode> infra = List.of(
                UserErrorCode.FIRST_NAME_EMPTY,
                UserErrorCode.EMAIL_EMPTY,
                UserErrorCode.EMAIL_EMPTY
        );
        List<UserErrorCode> domain = List.of(
                UserErrorCode.EMAIL_EMPTY,
                UserErrorCode.BASE_SALARY_INVALID
        );

        UserValidationException ex = new UserValidationException(infra, domain);

        List<UserErrorCode> expected = List.of(
                UserErrorCode.FIRST_NAME_EMPTY,
                UserErrorCode.EMAIL_EMPTY,
                UserErrorCode.BASE_SALARY_INVALID
        );

        assertThat(ex.all()).containsExactlyElementsOf(expected);

        assertThat(ex.errors()).containsExactlyElementsOf(expected);
        assertThat(ex.kind()).isEqualTo(ErrorKind.VALIDATION);
        assertThat(ex.getMessage()).isEqualTo("Errores de validación de usuario");

        assertThat(ex.getInfraErrors()).containsExactlyElementsOf(infra);
        assertThat(ex.getDomainErrors()).containsExactlyElementsOf(domain);
    }
}