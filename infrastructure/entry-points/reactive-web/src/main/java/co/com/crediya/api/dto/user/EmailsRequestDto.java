package co.com.crediya.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        description = "Lista de correos electrónicos para consultar usuarios",
        example = "[\"user1@crediya.com\", \"user2@crediya.com\"]",
        required = true
)
public record EmailsRequestDto(
        List<String> emails) {

}
