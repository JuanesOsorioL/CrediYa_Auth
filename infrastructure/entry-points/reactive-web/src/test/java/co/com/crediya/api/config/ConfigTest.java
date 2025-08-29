package co.com.crediya.api.config;

/*
import co.com.crediya.api.UserHandler;
import co.com.crediya.api.UserRouterRest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {UserRouterRest.class, UserHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}*/

/*

import co.com.crediya.api.UserHandler;
import co.com.crediya.api.UserRouterRest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest
@Import({UserRouterRest.class, CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserHandler userHandler;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        Mockito.when(userHandler.findAll(any()))
                .thenReturn(ServerResponse.ok().bodyValue("[]"));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}
*/

/*
import co.com.crediya.api.UserHandler;
import co.com.crediya.api.UserRouterRest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;

//@WebFluxTest
//@Import({UserRouterRest.class, CorsConfig.class, SecurityHeadersConfig.class})
@WebFluxTest
@Import({UserRouterRest.class, CorsConfig.class, SecurityHeadersConfig.class})
@ContextConfiguration
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserHandler userHandler;


    @Test
    void corsConfigurationShouldAllowOrigins() {
        Mockito.when(userHandler.findAll(any()))
                .thenReturn(ServerResponse.ok().bodyValue("[]"));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}*/