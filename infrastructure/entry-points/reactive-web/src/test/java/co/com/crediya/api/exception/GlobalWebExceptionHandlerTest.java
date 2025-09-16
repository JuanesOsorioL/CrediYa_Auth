package co.com.crediya.api.exception;


import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.usecase.exception.UserValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GlobalWebExceptionHandlerTest {

    private ApiResponseBuilder apiResponseBuilder;
    private DomainHttpStatusMapper statusMapper;
    private Logger logger;
    private GlobalWebExceptionHandler handler;

    @BeforeEach
    void setUp() {
        apiResponseBuilder = mock(ApiResponseBuilder.class);
        statusMapper = mock(DomainHttpStatusMapper.class);
        logger = mock(Logger.class);

        handler = new GlobalWebExceptionHandler(apiResponseBuilder, statusMapper, logger);
    }

    @Test
    void whenResponseCommitted_returnsMonoError() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));

        exchange.getResponse().setComplete().block();

        RuntimeException ex = new RuntimeException("already committed");

        StepVerifier.create(handler.handle(exchange, ex))
                .expectErrorMatches(t -> t == ex)
                .verify();

        verifyNoInteractions(apiResponseBuilder, statusMapper);
    }

    @Test
    void whenDomainException_buildsMappedErrorResponseAndWrites() {

        var infra = List.of(UserErrorCode.EMAIL_EMPTY);
        var domain = List.of(UserErrorCode.BASE_SALARY_INVALID);
        var de = new UserValidationException(infra, domain);

        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/domain"));

        when(statusMapper.toHttpStatus(eq(de.kind()))).thenReturn(HttpStatus.BAD_REQUEST);

        when(apiResponseBuilder.buildError(any(HttpStatus.class), anyString(), anyString(), any()))
                .thenAnswer(inv -> {
                    HttpStatus st = inv.getArgument(0);
                    Object body = inv.getArgument(3);
                    return ServerResponse.status(st).bodyValue(body);
                });

        StepVerifier.create(handler.handle(exchange, de))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ArgumentCaptor<String> codeCap = ArgumentCaptor.forClass(String.class);
        verify(apiResponseBuilder).buildError(eq(HttpStatus.BAD_REQUEST), codeCap.capture(), eq(de.getMessage()), any());
        assertThat(codeCap.getValue()).isEqualTo(UserErrorCode.GENERIC_ERROR.getCode());

        verify(statusMapper).toHttpStatus(de.kind());
    }

    @Test
    void whenNonDomainException_buildsInternalServerError() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/generic"));
        Throwable ex = new IllegalStateException("boom");

        when(apiResponseBuilder.buildError(eq(HttpStatus.INTERNAL_SERVER_ERROR),
                eq(UserErrorCode.GENERIC_ERROR.getCode()),
                anyString(),
                any()))
                .thenAnswer(inv -> {
                    HttpStatus st = inv.getArgument(0);
                    Object body = inv.getArgument(3);
                    return ServerResponse.status(st).bodyValue(body);
                });

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(apiResponseBuilder).buildError(
                eq(HttpStatus.INTERNAL_SERVER_ERROR),
                eq(UserErrorCode.GENERIC_ERROR.getCode()),
                eq("Error interno del servidor"),
                eq(List.of("Ocurrió un error inesperado"))
        );
        verifyNoInteractions(statusMapper);
    }
}