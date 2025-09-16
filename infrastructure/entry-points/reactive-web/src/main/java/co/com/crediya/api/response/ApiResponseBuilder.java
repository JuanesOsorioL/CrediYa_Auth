package co.com.crediya.api.response;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@AllArgsConstructor
public class ApiResponseBuilder {

    public <T> Mono<ServerResponse> build(HttpStatus status, String message, T body) {
        ApiRespons<T> response = new ApiRespons<>(status.value(), null, message, body);
        return ServerResponse.status(status)
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .bodyValue(response);
    }

    public <T> Mono<ServerResponse> buildError(HttpStatus status, String code, String message, T body) {
        ApiRespons<T> response = new ApiRespons<>(status.value(), code, message, body);
        return ServerResponse.status(status)
                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                .bodyValue(response);
    }
}
