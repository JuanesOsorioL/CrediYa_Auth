package co.com.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto (
        @NotBlank(message = "USR_006")
        @Email(message = "USR_003")
        String email,
        @NotBlank(message = "USR_011")
        String password
) {
}
