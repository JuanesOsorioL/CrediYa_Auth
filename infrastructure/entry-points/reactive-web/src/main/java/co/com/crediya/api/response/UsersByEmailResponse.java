package co.com.crediya.api.response;

import co.com.crediya.api.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        description = "Mapa de usuarios indexados por correo electrónico",
        example = "{ \"user1@crediya.com\": { \"firstName\": \"Juan\", \"lastName\": \"Pérez\", \"email\": \"user1@crediya.com\" } }"
)
public record UsersByEmailResponse(Map<String, UserDto> users) {


}