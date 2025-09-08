package co.com.crediya.model.user;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String documentId;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String password;
    private String rolId;
    private BigDecimal baseSalary;
}
