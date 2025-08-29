package co.com.crediya.api;


import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.exception.ApiResponseBuilder;
import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserService;
import co.com.crediya.usecase.user.exception.UserErrorCode;
import co.com.crediya.usecase.user.exception.UserValidationException;
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

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final ApiResponseBuilder apiResponseBuilder;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final Validator validator;
    private final GlobalLogger logger;


    private static final Map<String, UserErrorCode> CODE_TO_ERROR_MAP = Map.of(
            "USR_001", UserErrorCode.FIRST_NAME_EMPTY,
            "USR_002", UserErrorCode.LAST_NAME_EMPTY,
            "USR_003", UserErrorCode.EMAIL_INVALID,
            "USR_006", UserErrorCode.EMAIL_EMPTY,
            "USR_004", UserErrorCode.BASE_SALARY_INVALID,
            "USR_007", UserErrorCode.BASE_SALARY_EMPTY
    );

    private UserErrorCode mapMessageToErrorCode(String code) {
        return CODE_TO_ERROR_MAP.get(code);
    }


    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .doOnSubscribe(sub -> logger.info("Nueva petición para crear usuario"))
                .doOnNext(dto -> logger.info("DTO recibido"))
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();

                    if (!infraErrors.isEmpty()) {
                        logger.warn("Validación infra fallida -> errores");
                        return Mono.error(new UserValidationException(infraErrors, List.of()));
                    }
                    User user = userDtoMapper.toUser(dto);
                    logger.info("Validaciones correctas, transformado a dominio");
                    return userService.createUser(user)
                            .doOnSubscribe(sub -> logger.info("Invocando UserService.createUser"))
                            .doOnNext(u -> logger.info("Usuario persistido"))
                            .map(userDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("Usuario creado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.CREATED, "Usuario creado exitosamente", userDto));
    }


    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        return userService.getAllUsers()
                .doOnSubscribe(sub -> logger.info("findAll suscrito"))
                .doOnNext(u -> logger.info("Se retornan todos los Usuarios"))
                .map(userDtoMapper::toDto)
                .doOnNext(u -> logger.info("Se convierten a DTO"))
                .collectList()
                .doOnNext(u -> logger.info("Se agrupan en una Lista"))
                .flatMap(list -> apiResponseBuilder.build(HttpStatus.OK, "Usuarios recuperados exitosamente", list))
                .onErrorResume(e -> apiResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", List.of("Error al recuperar usuarios")));
    }


}
