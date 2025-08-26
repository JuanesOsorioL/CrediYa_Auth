package co.com.crediya.api;


import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.exception.ResourceNotFoundException;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
   // private final RequestValidator requestValidator;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserHandler.class);

    public Mono<ServerResponse> createUser(ServerRequest request) {
        log.info("Request received to register user");
        return request.bodyToMono(UserDto.class)
                .flatMap(dto -> {
                    User user = userDtoMapper.toUser(dto);
                    return userService.createUser(user);
                })

                .flatMap(savedUser -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(userDtoMapper.toDto(savedUser)))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        return userService.getAllUsers()
                .map(userDtoMapper::toDto)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }



    /*
    public Mono<ServerResponse> createUser(ServerRequest request) {
        log.info("Request received to register user");
        return request.bodyToMono(UserDto.class)
                //.flatMap(requestValidator::userValidate) // reactivar si usas validación
                .map(userDtoMapper::toUser)
                .flatMap(userService::createUser)
                .flatMap(savedUser -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(userDtoMapper.toDto(savedUser)))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("No se pudo crear el usuario")));
        // Nota: No capturar onErrorResume aquí para delegar manejo a global
    }
*/




}
