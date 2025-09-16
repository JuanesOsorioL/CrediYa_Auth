package co.com.crediya.api.dto.segurity;

public record ClaismoDto(
                String FistName,
                String sub,
                String Rol,
                String exp,
                String LastName,
                String Document,
                String iat,
                String jti) {}