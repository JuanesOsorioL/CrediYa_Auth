package co.com.crediya.api.segurity.filter;

import co.com.crediya.api.dto.login.TokenDto;
import co.com.crediya.api.segurity.jwt.AuthenticationService;
import co.com.crediya.model.exception.specific_exceptions.ForbiddenException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.usecase.logger.Logger;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private Logger logger;
    @Mock
    private WebFilterChain chain;

    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new AuthFilter(authenticationService, logger);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void whitelist_login_allowsWithoutToken() {
        var req = MockServerHttpRequest.get("/api/v1/login").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void whitelist_validateToken_allowsWithoutToken() {
        var req = MockServerHttpRequest.get("/api/v1/validateToken").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void whitelist_OPTIONS_allowsWithoutToken() {
        var req = MockServerHttpRequest.options("/api/v1/usuarios/anything").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void missingAuthorizationHeader_returnsUnauthorized() {
        var req = MockServerHttpRequest.get("/api/v1/usuarios/list").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(chain, never()).filter(any());
    }

    @Test
    void wrongAuthorizationPrefix_returnsUnauthorized() {
        var req = MockServerHttpRequest.get("/api/v1/usuarios/list")
                .header("Authorization", "Token abc123")
                .build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(UnauthorizedException.class)
                .verify();

        verifyNoInteractions(authenticationService);
        verify(chain, never()).filter(any());
    }

    @Test
    void invalidToken_returnsUnauthorized() {
        var req = MockServerHttpRequest.get("/api/v1/usuarios/list")
                .header("Authorization", "Bearer badtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(null);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(chain, never()).filter(any());
    }

    @Test
    void adminRole_canAccessUsuariosEndpoints() {
        var req = MockServerHttpRequest
                .get("/api/v1/usuarios/list")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        Claims claims = mock(Claims.class);
        when(claims.get(eq("Rol"), eq(String.class))).thenReturn("Admin");
        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void adviserRole_canAccessUsuariosEndpoints() {
        var req = MockServerHttpRequest
                .get("/api/v1/usuarios/create")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        Claims claims = mock(Claims.class);
        when(claims.get(eq("Rol"), eq(String.class))).thenReturn("Adviser");
        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void customerRole_canAccessSolicitudEndpoints() {
        var req = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/v1/solicitud/create")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);


        Claims claims = mock(Claims.class);
        when(claims.get(eq("Rol"), eq(String.class))).thenReturn("Customer");
        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void forbiddenRole_onUsuarios_returnsForbidden() {
        var req = MockServerHttpRequest
                .get("/api/v1/usuarios/list")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        Claims claims = mock(Claims.class);
        when(claims.get(eq("Rol"), eq(String.class))).thenReturn("Customer");
        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(ForbiddenException.class)
                .verify();

        verify(chain, never()).filter(any());
    }

    @Test
    void customerRole_cannotAccessUnknownEndpoints_forbidden() {
        var req = MockServerHttpRequest.get("/api/v1/otro/recurso").header("Authorization", "Bearer tkn").build();
        var exchange = MockServerWebExchange.from(req);

        Claims claims = mock(Claims.class);
        when(claims.get(eq("Rol"), eq(String.class))).thenReturn("Customer");
        when(authenticationService.validateTokenAndGetClaims(any(TokenDto.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(ForbiddenException.class)
                .verify();

        verify(chain, never()).filter(any());
    }
}