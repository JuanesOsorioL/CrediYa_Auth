package co.com.crediya.api.segurity;


import co.com.crediya.api.dto.segurity.TokenDto;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.model.exception.specific_exceptions.ForbiddenException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    @Mock
    private SegurityGateway segurityGateway;
    @Mock
    private GenericDtoMapper genericDtoMapper;
    @Mock
    private Logger logger;
    @Mock
    private WebFilterChain chain;

    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new AuthFilter(segurityGateway, genericDtoMapper, logger);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void whitelist_login_allowsWithoutToken() {
        var req = MockServerHttpRequest.get("/api/v1/login").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
        verifyNoInteractions(segurityGateway, genericDtoMapper);
    }

    @Test
    void whitelist_validateToken_allowsWithoutToken() {
        var req = MockServerHttpRequest.get("/api/v1/validateToken").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
        verifyNoInteractions(segurityGateway, genericDtoMapper);
    }

    @Test
    void whitelist_OPTIONS_allowsWithoutToken() {
        var req = MockServerHttpRequest.options("/api/v1/usuarios/anything").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
        verifyNoInteractions(segurityGateway, genericDtoMapper);
    }

    @Test
    void missingAuthorizationHeader_returnsUnauthorized() {
        var req = MockServerHttpRequest.get("/api/v1/usuarios/list").build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(chain, never()).filter(any());
        verifyNoInteractions(segurityGateway, genericDtoMapper);
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

        verifyNoInteractions(segurityGateway, genericDtoMapper);
        verify(chain, never()).filter(any());
    }

    @Test
    void invalidToken_returnsUnauthorized() {
        var req = MockServerHttpRequest.get("/api/v1/usuarios/list")
                .header("Authorization", "Bearer badtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("badtoken"));
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(null);

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

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("goodtoken"));
        var claims = new Claismo("Ana", "ana@ex.com", "Admin", "1700000000",
                "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void adviserRole_canAccessUsuariosEndpoints() {
        var req = MockServerHttpRequest
                .get("/api/v1/usuarios/create")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("goodtoken"));
        var claims = new Claismo("Ana", "ana@ex.com", "Adviser", "1700000000",
                "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void customerRole_canAccessSolicitudEndpoints() {
        var req = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/v1/solicitud/create")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("goodtoken"));
        var claims = new Claismo("Ana", "ana@ex.com", "Customer", "1700000000",
                "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void forbiddenRole_onUsuarios_returnsForbidden() {
        var req = MockServerHttpRequest
                .get("/api/v1/usuarios/list")
                .header("Authorization", "Bearer goodtoken")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("goodtoken"));
        var claims = new Claismo("Ana", "ana@ex.com", "Customer", "1700000000",
                "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(ForbiddenException.class)
                .verify();

        verify(chain, never()).filter(any());
    }

    @Test
    void customerRole_cannotAccessUnknownEndpoints_forbidden() {
        var req = MockServerHttpRequest
                .get("/api/v1/otro/recurso")
                .header("Authorization", "Bearer tkn")
                .build();
        var exchange = MockServerWebExchange.from(req);

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("tkn"));
        var claims = new Claismo("Ana", "ana@ex.com", "Customer", "1700000000",
                "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(claims);

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(ForbiddenException.class)
                .verify();

        verify(chain, never()).filter(any());
    }
}