package co.com.crediya.r2dbc.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEntity {
    @Id
    @Column("id_usuario")
    private String userId;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column("birth_date")
    private LocalDate birthDate;
    private String phone;
    @Column("document_id")
    private String documentId;
    private String email;
    @Column("base_salary")
    private BigDecimal baseSalary;
}
