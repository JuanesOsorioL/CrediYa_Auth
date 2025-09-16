package co.com.crediya.api.dto.rol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolDtoTest {

    @Test
    void constructorAndAccessors() {
        RolDto dto = new RolDto("ROL_1", "Admin", "Administrador del sistema");
        assertThat(dto.rolId()).isEqualTo("ROL_1");
        assertThat(dto.name()).isEqualTo("Admin");
        assertThat(dto.description()).isEqualTo("Administrador del sistema");
    }

    @Test
    void equalsHashCodeAndToString() {
        RolDto a = new RolDto("ROL_1", "Admin", "Administrador");
        RolDto b = new RolDto("ROL_1", "Admin", "Administrador");
        RolDto c = new RolDto("ROL_2", "User", "Usuario");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);

        assertThat(a.toString()).contains("ROL_1", "Admin", "Administrador");
    }
}