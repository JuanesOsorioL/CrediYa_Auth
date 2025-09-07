package co.com.crediya.model.rol;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Rol {
    private String rolId;
    private String name;
    private String description;
}
