package co.com.crediya.api;


import co.com.crediya.api.dto.CreateUserDto;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserHandler.class);


    public Mono<ServerResponse> createUser(ServerRequest request) {
        log.info("Request received to register user");
        return request.bodyToMono(CreateUserDto.class)
                .flatMap(dto -> {
                    User user = userDtoMapper.toUser(dto);
                    return userService.createUser(user);
                })
                .flatMap(savedUser -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(userDtoMapper.toDto(savedUser)))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }














/*
//private  final UseCase useCase;
//private  final UseCase2 useCase2;

    public Mono<ServerResponse> listenGETUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        // useCase2.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }

    */

}
