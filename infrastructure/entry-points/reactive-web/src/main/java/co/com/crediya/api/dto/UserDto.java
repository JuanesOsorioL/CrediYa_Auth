package co.com.crediya.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDto(String id,String firstName,String lastName,LocalDate birthDate,String address,String phone,String email,BigDecimal baseSalary){
}
