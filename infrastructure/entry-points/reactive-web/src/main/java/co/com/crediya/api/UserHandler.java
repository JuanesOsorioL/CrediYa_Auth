package co.com.crediya.api;


import co.com.crediya.api.dto.ApiResponse;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.exception.ValidationException;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserService;
import co.com.crediya.usecase.user.exception.DomainValidationException;
import co.com.crediya.usecase.user.exception.UserErrorCode;
import co.com.crediya.usecase.user.exception.UserValidationResult;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final Validator validator;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserHandler.class);

    private static final Map<String, UserErrorCode> CODE_TO_ERROR_MAP = Map.of(
            "USR_001", UserErrorCode.FIRST_NAME_EMPTY,
            "USR_002", UserErrorCode.LAST_NAME_EMPTY,
            "USR_003", UserErrorCode.EMAIL_INVALID,
            "USR_006", UserErrorCode.EMAIL_EMPTY,
            "USR_004", UserErrorCode.BASE_SALARY_INVALID,
            "USR_007", UserErrorCode.BASE_SALARY_EMPTY
    );


    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    User user = userDtoMapper.toUser(dto);
                    return Mono.just(new UserValidationResult(user, infraErrors, List.of()));
                })
                .flatMap(result -> {
                    User user = result.getUser();
                    List<UserErrorCode> infraErrors = result.getInfraErrors();

                    return userService.createUser(user)
                            .map(savedUser -> new UserValidationResult(savedUser, infraErrors, List.of()))
                            .onErrorResume(DomainValidationException.class, dve ->
                                    Mono.just(new UserValidationResult(user, infraErrors, dve.getErrors()))
                            );
                })
                .flatMap(result -> {
                    log.info("errores de dominio: {}", result.getDomainErrors());
                    List<UserErrorCode> allErrors = Stream.concat(result.getInfraErrors().stream(), result.getDomainErrors().stream())
                            .distinct()
                            .toList();
                    log.info("todos los errores {}", allErrors);
                    if (!allErrors.isEmpty()) {
                        return buildResponse(HttpStatus.BAD_REQUEST, "Errores de validación",
                                allErrors.stream().map(UserErrorCode::getMessage).toList());
                    }

                    return buildResponse(HttpStatus.CREATED, "Usuario creado exitosamente",
                            userDtoMapper.toDto(result.getUser()));

                })
                .onErrorResume(e -> {
                    if (e instanceof ValidationException ve) {
                        return buildResponse(HttpStatus.BAD_REQUEST, "Errores en la entrada", ve.getErrors());
                    } else if (e instanceof DomainValidationException dve) {
                        return buildResponse(HttpStatus.BAD_REQUEST, "Errores de negocio", dve.getErrors());
                    } else {
                        log.error("Error inesperado", e);
                        return
                                buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", List.of(UserErrorCode.GENERIC_ERROR));
                    }
                });
    }


    private <T> Mono<ServerResponse> buildResponse(HttpStatus status, String message, T body) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setBody(body);
        return ServerResponse.status(status).bodyValue(response);
    }


    private UserErrorCode mapMessageToErrorCode(String code) {
        return CODE_TO_ERROR_MAP.get(code);
    }


    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        return userService.getAllUsers().map(userDtoMapper::toDto).collectList().flatMap(list -> {
            ApiResponse<List<UserDto>> response = new ApiResponse<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Usuarios recuperados exitosamente");
            response.setBody(list);
            return ServerResponse.ok().bodyValue(response);
        });
    }
}
