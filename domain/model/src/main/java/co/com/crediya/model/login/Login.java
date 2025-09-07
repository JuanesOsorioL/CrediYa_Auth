package co.com.crediya.model.login;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Login {
    private String email;
    private String password;
}
