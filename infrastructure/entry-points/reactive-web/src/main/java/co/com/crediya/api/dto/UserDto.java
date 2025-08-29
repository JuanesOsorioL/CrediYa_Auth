package co.com.crediya.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public record UserDto(
        String userId,
        @NotBlank(message = "USR_001")
        String firstName,
        @NotBlank(message = "USR_002")
        String lastName,
        LocalDate birthDate,
        String phone,
        @NotBlank(message = "USR_006")
        @Email(message = "USR_003")
        String email,
        @NotNull(message = "USR_007")
        @DecimalMin(value = "0.0", message = "USR_004")
        @DecimalMax(value = "15000000.0", message = "USR_004")
        BigDecimal baseSalary) {
}
