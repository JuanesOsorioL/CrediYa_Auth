package co.com.crediya.api;


import co.com.crediya.api.dto.ApiResponse;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserHandler.class);

    public Mono<ServerResponse> createUser(ServerRequest request) {
        log.info("Request received to register user");
        return request.bodyToMono(UserDto.class)
                .flatMap(dto -> {
                    User user = userDtoMapper.toUser(dto);
                    return userService.createUser(user);
                })
                .flatMap(savedUser -> {
                    ApiResponse<UserDto> response = new ApiResponse<>();
                    response.setStatus(HttpStatus.CREATED.value());
                    response.setMessage("Usuario creado exitosamente");
                    response.setBody(userDtoMapper.toDto(savedUser));
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(response);
                })
                .onErrorResume(e -> {
                    ApiResponse<String> errorResponse = new ApiResponse<>();
                    errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                    errorResponse.setMessage("Usuario no fue creado");
                    errorResponse.setBody(e.getMessage());
                    return ServerResponse.badRequest().bodyValue(errorResponse);
                });
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        return userService.getAllUsers()
                .map(userDtoMapper::toDto)
                .collectList()
                .flatMap(list -> {
                    ApiResponse<List<UserDto>> response = new ApiResponse<>();
                    response.setStatus(HttpStatus.OK.value());
                    response.setMessage("Usuarios recuperados exitosamente");
                    response.setBody(list);
                    return ServerResponse.ok().bodyValue(response);
                });
    }
}
