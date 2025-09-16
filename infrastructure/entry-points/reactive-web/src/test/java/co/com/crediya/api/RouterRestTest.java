package co.com.crediya.api;

import co.com.crediya.api.config.UserPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class RouterRestTest {

    private UserHandler handler;
    private UserPath userPath;
    private RouterRest routerRest;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = mock(UserHandler.class);
        userPath = mock(UserPath.class);
        routerRest = new RouterRest();

        when(userPath.getBase()).thenReturn("/api/v1/usuarios");
        when(userPath.getSomeUsers()).thenReturn("/api/v1/usuarios/mapa");
        when(userPath.getAllUsers()).thenReturn("/api/v1/usuarios/all");
        when(userPath.getDocument()).thenReturn("/api/v1/usuarios/documento");
        when(userPath.getLogin()).thenReturn("/api/v1/login");
        when(userPath.getValidateToken()).thenReturn("/api/v1/validateToken");

        when(handler.createUser(any())).thenReturn(ServerResponse.status(HttpStatus.CREATED).build());
        when(handler.getUsersMapEmails(any())).thenReturn(ServerResponse.ok().build());
        when(handler.findAll(any())).thenReturn(ServerResponse.ok().build());
        when(handler.findByDocumentId(any())).thenReturn(ServerResponse.ok().build());
        when(handler.login(any())).thenReturn(ServerResponse.ok().build());
        when(handler.validateToken(any())).thenReturn(ServerResponse.ok().build());

        RouterFunction<ServerResponse> router = routerRest.routerFunction(handler, userPath);
        client = WebTestClient.bindToRouterFunction(router).configureClient().build();
    }

    @Test
    void POST_base_createUser_returns201() {
        client.post()
                .uri("/api/v1/usuarios")
                .contentType(APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isCreated();

        verify(handler, times(1)).createUser(any());
    }

    @Test
    void POST_someUsers_getUsersMapEmails_returns200() {
        client.post()
                .uri("/api/v1/usuarios/mapa")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"emails\":[\"a@ex.com\"]}")
                .exchange()
                .expectStatus().isOk();

        verify(handler, times(1)).getUsersMapEmails(any());
    }

    @Test
    void GET_allUsers_findAll_returns200() {
        client.get()
                .uri("/api/v1/usuarios/all")
                .exchange()
                .expectStatus().isOk();

        verify(handler, times(1)).findAll(any());
    }

    @Test
    void POST_document_findByDocumentId_returns200() {
        client.post()
                .uri("/api/v1/usuarios/documento")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"documentId\":\"DOC-1\"}")
                .exchange()
                .expectStatus().isOk();

        verify(handler, times(1)).findByDocumentId(any());
    }

    @Test
    void POST_login_returns200() {
        client.post()
                .uri("/api/v1/login")
                .contentType(APPLICATION_JSON)
                .bodyValue("{\"email\":\"user@ex.com\",\"password\":\"x\"}")
                .exchange()
                .expectStatus().isOk();

        verify(handler, times(1)).login(any());
    }

    @Test
    void GET_validateToken_returns200() {
        client.get()
                .uri("/api/v1/validateToken")
                .exchange()
                .expectStatus().isOk();

        verify(handler, times(1)).validateToken(any());
    }
}