package co.com.crediya.api;


import co.com.crediya.api.dto.LoginDto;
import co.com.crediya.api.dto.UserDocumentDto;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.api.segurity.AuthenticationService;
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
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final ApiResponseBuilder apiResponseBuilder;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final Validator validator;
    private final GlobalLogger logger;
    private final AuthenticationService authenticationService;

    private UserErrorCode mapMessageToErrorCode(String code) {
        return UserErrorCode.fromCode(code);
    }


    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserDto.class)
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


    public Mono<ServerResponse> findByDocumentId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserDocumentDto.class)
                .doOnSubscribe(sub -> logger.info("Nueva petición para consultar cliente con documento"))
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
                    return userService.findByDocumentId(user.getDocumentId())
                            .doOnSubscribe(sub -> logger.info("Invocando UserService.findByDocumentId"))
                            .switchIfEmpty(Mono.error(new UserValidationException(List.of(UserErrorCode.USER_NOT_FOUND), List.of())))
                            .doOnNext(u -> logger.info("Usuario encontrado"))
                            .map(userDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("Usuario consultado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.OK, "Usuario encontrado exitosamente", userDto));
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



    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginDto.class)
                .doOnSubscribe(sub -> logger.info("Nueva petición para consultar si las credenciales son validas"))
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
                    return Mono.just(dto);
                }).map(userDtoMapper::toUserL)
                .flatMap(userService::findIsExist)
                .doOnSubscribe(sub -> logger.info("findIsExist suscrito  "))
                .map(authenticationService::generateToken)
                .doOnSubscribe(sub -> logger.info("findIsExist suscrito"))
                .doOnNext(u -> logger.info("Se retorna el Usuario"))
                .doOnSuccess(string -> logger.info("Usuario consultado exitosamente "))
                .flatMap(stringkey -> apiResponseBuilder.build(HttpStatus.CREATED, "Usuario creado exitosamente ", stringkey));


    }

}
