package co.com.crediya.usecase.user.exception;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    GENERIC_ERROR("USR_999", "Por favor intente luego."),
    FIRST_NAME_EMPTY("USR_001", "El nombre no puede ser vacío"),
    LAST_NAME_EMPTY("USR_002", "El apellido no puede ser vacío"),
    EMAIL_INVALID("USR_003", "El correo electrónico no es válido"),
    BASE_SALARY_INVALID("USR_004", "El salario base debe estar entre 0 y 15.000.000"),
    EMAIL_ALREADY_REGISTERED("USR_005", "El correo electrónico ya está registrado"),
    BASE_SALARY_EMPTY("USR_007", "El salario base no puede ser vacío"),
    EMAIL_EMPTY("USR_006", "El correo electrónico no puede ser vacío");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
