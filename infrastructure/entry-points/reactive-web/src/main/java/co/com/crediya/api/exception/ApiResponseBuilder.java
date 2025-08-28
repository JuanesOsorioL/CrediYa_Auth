package co.com.crediya.api.exception;

import co.com.crediya.api.dto.ApiRespons;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ApiResponseBuilder {
    public <T> Mono<ServerResponse> build(HttpStatus status, String message, T body) {
        ApiRespons<T> response = new ApiRespons<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setBody(body);

        return ServerResponse.status(status).bodyValue(response);
    }
}
