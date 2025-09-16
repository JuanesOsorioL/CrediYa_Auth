package co.com.crediya.api.dto.segurity;

import jakarta.validation.constraints.NotBlank;

public record TokenDto(
        @NotBlank(message = "USR_013")
        String token) {
}
