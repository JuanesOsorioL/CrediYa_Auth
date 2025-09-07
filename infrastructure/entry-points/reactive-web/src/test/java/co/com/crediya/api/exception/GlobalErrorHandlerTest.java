package co.com.crediya.api.exception;

import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.usecase.exception.UserValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private GlobalErrorHandler globalErrorHandler;
    private ApiResponseBuilder apiResponseBuilder;
    GlobalLogger logger;

    @BeforeEach
    void setUp() {
        apiResponseBuilder = new ApiResponseBuilder();
        logger = Mockito.mock(GlobalLogger.class);
        globalErrorHandler = new GlobalErrorHandler(apiResponseBuilder,logger);
    }

    @Test
    void shouldHandleUserValidationException() {
        UserValidationException ex = new UserValidationException(
                List.of(UserErrorCode.EMAIL_EMPTY, UserErrorCode.LAST_NAME_EMPTY),
                List.of(UserErrorCode.BASE_SALARY_INVALID)
        );

        var handler = globalErrorHandler.filter();

        Mono<ServerResponse> responseMono = handler
                .filter(null, request -> Mono.error(ex));

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleGenericException() {

        RuntimeException ex = new RuntimeException("Boom!");
        var handler = globalErrorHandler.filter();

        Mono<ServerResponse> responseMono = handler
                .filter(null, request -> Mono.error(ex));

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .verifyComplete();
    }
}