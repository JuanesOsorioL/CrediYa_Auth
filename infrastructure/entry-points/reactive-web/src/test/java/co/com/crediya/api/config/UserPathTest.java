package co.com.crediya.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPathTest {
    @Test
    void testGetterAndSetter() {
        UserPath userPath = new UserPath();
        userPath.setBase("/api/v1/usuarios");

        assertThat(userPath.getBase()).isEqualTo("/api/v1/usuarios");
    }
}