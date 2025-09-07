package co.com.crediya.r2dbc.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("rol")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolEntity {
    @Id
    @Column("rol_id")
    private String rolId;
    @Column("name")
    private String name;
    @Column("description")
    private String description;

}
