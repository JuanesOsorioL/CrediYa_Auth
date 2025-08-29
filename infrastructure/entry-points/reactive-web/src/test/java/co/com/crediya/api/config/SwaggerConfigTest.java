package co.com.crediya.api.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void testSwaggerConfigLoads() {
        SwaggerConfig config = new SwaggerConfig();
        assertThat(config).isNotNull();
    }
}