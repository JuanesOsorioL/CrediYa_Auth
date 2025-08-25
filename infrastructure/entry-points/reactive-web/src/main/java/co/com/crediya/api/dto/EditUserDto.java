package co.com.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditUserDto(
        String userId,
        @NotBlank(message = "El nombre no puede ser vacío")
        String firstName,
        @NotBlank(message = "El apellido no puede ser vacío")
        String lastName,
        LocalDate birthDate,
        String address,
        String phone,
        @NotBlank(message = "El correo electrónico no puede ser vacío")
        @Email(message = "El correo electrónico debe ser válido")
        String email,
        @NotNull(message = "El salario base es obligatorio")
        BigDecimal baseSalary){
}
