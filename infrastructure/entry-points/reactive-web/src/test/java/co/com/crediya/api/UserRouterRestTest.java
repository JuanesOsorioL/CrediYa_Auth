package co.com.crediya.api;

import co.com.crediya.api.config.UserPath;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.exception.GlobalErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserRouterRestTest {

    private WebTestClient webTestClient;
    private UserHandler userHandler;
    private GlobalErrorHandler errorHandler;
    private UserPath userPath;

    @BeforeEach
    void setUp() {
        userHandler = Mockito.mock(UserHandler.class);
        errorHandler = Mockito.mock(GlobalErrorHandler.class);
        userPath = Mockito.mock(UserPath.class);

        when(userPath.getBase()).thenReturn("/api/v1/usuarios");
        when(userPath.getBase()).thenReturn("/api/v1/usuarios");
        when(userPath.getBase()).thenReturn("/api/v1/usuarios");

        try {
            when(userPath.getDocument()).thenReturn("/api/v1/usuarios/{id}");
        } catch (Throwable ignored) {
        }
        try {
            when(userPath.getDocument()).thenReturn("/api/v1/usuarios/{id}");
        } catch (Throwable ignored) {
        }
        try {
            when(userPath.getDocument()).thenReturn("/api/v1/usuarios/{id}");
        } catch (Throwable ignored) {
        }

        when(errorHandler.filter()).thenReturn((request, next) -> next.handle(request));

        UserRouterRest routerRest = new UserRouterRest();
        RouterFunction<ServerResponse> routerFunction =
                routerRest.routerFunction(userHandler, errorHandler, userPath);

        this.webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void testGETUsuariosRoute() {
        when(userHandler.findAll(any()))
                .thenReturn(ServerResponse.ok().bodyValue("[]"));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("[]");
    }

    @Test
    void testPOSTUsuariosRoute() {
        UserDto requestDto = new UserDto(
                "u01",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com",
                "123456789",
                BigDecimal.ONE
        );

        when(userHandler.createUser(any()))
                .thenReturn(ServerResponse.status(HttpStatus.CREATED).build());

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isCreated();
    }
}