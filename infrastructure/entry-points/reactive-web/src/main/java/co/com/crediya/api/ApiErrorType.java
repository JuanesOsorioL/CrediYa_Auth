package co.com.crediya.api;

public enum ApiErrorType {
    BAD_REQUEST(400, "Parámetros inválidos"),
    NOT_FOUND(404, "Recurso no encontrado"),
    INTERNAL_SERVER_ERROR(500, "Error interno del servidor"),
    CONFLICT(409, "Conflicto con el recurso");

    private final int code;
    private final String message;

    ApiErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() { return code; }
    public String message() { return message; }
}
