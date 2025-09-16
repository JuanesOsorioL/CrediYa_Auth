package co.com.crediya.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void openAPI_containsBearerSecurityScheme_andRequirement() {
        SwaggerConfig cfg = new SwaggerConfig();
        OpenAPI api = cfg.openAPI();

        assertThat(api.getComponents()).isNotNull();
        assertThat(api.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

        SecurityScheme sch = api.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(sch.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(sch.getScheme()).isEqualTo("bearer");
        assertThat(sch.getBearerFormat()).isEqualTo("JWT");

        assertThat(api.getSecurity()).isNotEmpty();
        assertThat(api.getSecurity().get(0).get("bearerAuth")).isNotNull();
    }
}