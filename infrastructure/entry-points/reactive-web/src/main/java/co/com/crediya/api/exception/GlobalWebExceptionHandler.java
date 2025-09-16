package co.com.crediya.api.exception;

import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.model.exception.DomainException;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.logger.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final ApiResponseBuilder apiResponseBuilder;
    private final DomainHttpStatusMapper statusMapper;
    private final Logger logger;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.info("GlobalWebExceptionHandler -> handle : inicio de flujo.");
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        Throwable cause = Exceptions.unwrap(ex);

        if (cause instanceof DomainException de) {
            var status = statusMapper.toHttpStatus(de.kind());
            var code = de.code() != null ? de.code() : UserErrorCode.GENERIC_ERROR.getCode();
            var details = de.errors().isEmpty()
                    ? List.of(de.getMessage())
                    : de.errors().stream().map(UserErrorCode::getMessage).distinct().toList();

            logger.warn("GlobalWebExceptionHandler -> handle : DomainException capturada. kind = " + de.kind() + ", code = " + code + ", status = " + status + " ");

            return apiResponseBuilder.buildError(status, code, de.getMessage(), details)
                    .flatMap(resp -> resp.writeTo(exchange, new ResponseContext()));
        }

        logger.info("GlobalWebExceptionHandler -> handle : Error interno del servidor");
        return apiResponseBuilder.buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        UserErrorCode.GENERIC_ERROR.getCode(),
                        "Error interno del servidor",
                        List.of("Ocurrió un error inesperado"))
                .flatMap(resp -> resp.writeTo(exchange, new ResponseContext()));
    }

    static final class ResponseContext implements ServerResponse.Context {
        private final HandlerStrategies strategies = HandlerStrategies.withDefaults();

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return strategies.messageWriters();
        }

        @Override
        public List<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers() {
            return strategies.viewResolvers();
        }
    }
}
