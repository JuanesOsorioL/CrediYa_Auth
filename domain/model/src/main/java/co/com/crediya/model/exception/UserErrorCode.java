package co.com.crediya.model.exception;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum UserErrorCode {
    GENERIC_ERROR("USR_999", "Por favor intente luego."),
    FIRST_NAME_EMPTY("USR_001", "El nombre no puede ser vacío"),
    LAST_NAME_EMPTY("USR_002", "El apellido no puede ser vacío"),
    EMAIL_INVALID("USR_003", "El correo electrónico no es válido"),
    BASE_SALARY_INVALID("USR_004", "El salario base debe estar entre 0 y 15.000.000"),
    EMAIL_ALREADY_REGISTERED("USR_005", "El correo electrónico ya está registrado"),
    BASE_SALARY_EMPTY("USR_007", "El salario base no puede ser vacío"),
    DOCUMENT_EMPTY("USR_008", "El Documento no puede ser vacío"),
    USER_NOT_FOUND("USR_009", "Cliente no encontrado"),
    DOCUMENT_ALREADY_REGISTERED("USR_010", "El Documento ingresado ya existe"),
    PASSWORD_EMPTY("USR_011", "El Password no puede ser vacío"),
    ROL_EMPTY("USR_012", "El Rol no puede ser vacío"),
    ROL_NOT_FOUND("USR_013", "Rol no encontrado"),
    TOKEN_EMPTY("USR_014", "Token no proporcionado"),
    TOKEN_INVALID("USR_015", "Token invalido"),
    YOU_DONT_HAVE_PERMISSION("USR_016", "No tienes permisos para acceder a este recurso"),
    EMAIL_EMPTY("USR_006", "El correo electrónico no puede ser vacío");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private static final Map<String, UserErrorCode> CODE_MAP = Stream.of(values())
            .collect(Collectors.toMap(UserErrorCode::getCode, e -> e));

    public static UserErrorCode fromCode(String code) {
        return CODE_MAP.get(code);
    }
}
