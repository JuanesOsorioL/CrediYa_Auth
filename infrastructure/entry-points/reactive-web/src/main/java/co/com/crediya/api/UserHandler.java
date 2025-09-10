package co.com.crediya.api;


import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.login.TokenClaimsDto;
import co.com.crediya.api.dto.login.TokenDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.api.segurity.AuthenticationService;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.rol.gateways.RolService;
import co.com.crediya.usecase.user.gateways.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final ApiResponseBuilder apiResponseBuilder;
    private final UserService userService;
    private final RolService rolService;
    private final GenericDtoMapper genericDtoMapper;
    private final Validator validator;
    private final GlobalLogger logger;
    private final AuthenticationService authenticationService;


    private static final ParameterizedTypeReference<Set<String>> LIST_STRING =
            new ParameterizedTypeReference<>() {
            };


    private UserErrorCode mapMessageToErrorCode(String code) {
        return UserErrorCode.fromCode(code);
    }

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        logger.info("UserHandler -> login : inicia el flujo.");
        return serverRequest.bodyToMono(LoginDto.class)
                .doOnNext(dto -> logger.info("DTO recibido"))
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    if (!infraErrors.isEmpty()) {
                        logger.warn("Se encontraron errores de validación en el DTO");
                        return Mono.error(new UserValidationException(infraErrors, List.of()));
                    }
                    return Mono.just(dto);
                })
                .map(genericDtoMapper::toUser)
                .doOnNext(login -> logger.info("se mapea de dto a usuario"))
                .flatMap(userService::findIsExist)
                .doOnNext(user -> logger.info("Se llama a el caso de uso para verifica si el usuario existe"))
                .flatMap(user -> rolService.findById(user.getRolId())
                        .doOnNext(rol -> logger.info("Se llama a el caso de uso de rol para obtener el tipo de rol"))
                        .map(role -> new TokenClaimsDto(
                                user.getUserId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getDocumentId(),
                                user.getEmail(),
                                role.getName()
                        ))).doOnNext(tokenclaismodto -> logger.info("se crea un TokenClaimsDto"))
                .flatMap(tokenClaimsDto -> {
                    TokenDto token = authenticationService.generateToken(tokenClaimsDto);
                    return apiResponseBuilder.build(HttpStatus.OK, "Usuario logueado exitosamente", token.token());
                })
                .doOnSuccess(l -> logger.info("Usuario autenticado exitosamente"));
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        logger.info("UserHandler -> findAll : inicia el flujo.");
        return userService.getAllUsers()
                .doOnNext(user -> logger.info("UserHandler -> findAll : se llama a userService.getAllUsers"))
                .doOnNext(user -> logger.info("Se retornan todos los Usuarios"))
                .map(genericDtoMapper::toDto)
                .collectList()
                .flatMap(list -> {
                    logger.info("Se recuperaron y transformaron todos los usuarios a DTO");
                    return apiResponseBuilder.build(HttpStatus.OK, "Usuarios recuperados exitosamente", list);
                })
                .doOnSuccess(resp -> logger.info("Todos los usuarios fueron recuperados y enviados correctamente"));
    }

    public Mono<ServerResponse> findByDocumentId(ServerRequest serverRequest) {
        logger.info("UserHandler -> findByDocumentId : inicia el flujo.");
        return serverRequest.bodyToMono(UserDocumentDto.class)
                .doOnNext(sub -> logger.info("UserHandler -> findByDocumentId : Nueva petición para consultar cliente con documento"))
                .doOnNext(dto -> logger.info("UserHandler -> findByDocumentId : DTO recibido"))
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    if (!infraErrors.isEmpty()) {
                        logger.warn("UserHandler -> findByDocumentId : Validación infra fallida -> errores");
                        return Mono.error(new UserValidationException(infraErrors, List.of()));
                    }
                    User user = genericDtoMapper.toUser(dto);
                    logger.info("UserHandler -> findByDocumentId : Validaciones correctas, transformado a dominio");
                    return userService.findByDocumentId(user.getDocumentId())
                            .doOnSubscribe(sub -> logger.info("UserHandler -> findByDocumentId : Invocando UserService.findByDocumentId"))
                            .switchIfEmpty(Mono.error(new UserValidationException(List.of(UserErrorCode.USER_NOT_FOUND), List.of())))
                            .doOnNext(u -> logger.info("UserHandler -> findByDocumentId : Usuario encontrado"))
                            .map(genericDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("UserHandler -> findByDocumentId : Usuario consultado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.OK, "Usuario encontrado exitosamente", userDto));
    }


    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        logger.info("UserHandler -> createUser : inicia el flujo.");
        return serverRequest.bodyToMono(UserDto.class)
                .doOnSubscribe(sub -> logger.info("UserHandler -> createUser : Nueva petición para crear usuario"))
                .doOnNext(dto -> logger.info("UserHandler -> createUser : DTO recibido"))
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    if (!infraErrors.isEmpty()) {
                        logger.warn("UserHandler -> createUser : Validación infra fallida -> errores");
                        return Mono.error(new UserValidationException(infraErrors, List.of()));
                    }
                    User user = genericDtoMapper.toUser(dto);
                    logger.info("UserHandler -> createUser : Validaciones correctas, transformado a dominio");
                    return userService.createUser(user)
                            .doOnSubscribe(sub -> logger.info("UserHandler -> createUser : Invocando UserService.createUser"))
                            .doOnNext(u -> logger.info("UserHandler -> createUser : Usuario persistido"))
                            .map(genericDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("UserHandler -> createUser : Usuario creado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.CREATED, "Usuario creado exitosamente", userDto));
    }


    public Mono<ServerResponse> validateToken(ServerRequest serverRequest) {
        logger.info("UserHandler -> validateToken : inicia el flujo.");

        String authHeader = serverRequest.headers()
                .header("Authorization")
                .stream()
                .filter(header -> header.startsWith("Bearer "))
                .findFirst()
                .orElse(null);

        if (authHeader == null || authHeader.isBlank()) {
            logger.info("UserHandler -> validateToken : Token no proporcionado");
            return apiResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Token no proporcionado", null);
        }

        String tokenSinBearer = authHeader.replace("Bearer ", "").trim();
        TokenDto tokenDto = new TokenDto(tokenSinBearer);

        return Mono.just(tokenDto)
                .doOnNext(token -> logger.info("UserHandler -> validateToken : Iniciando validación del token"))
                .flatMap(tokenDton -> {
                    Claims claims = authenticationService.validateTokenAndGetClaims(tokenDton);
                    if (claims == null) {

                        logger.info("UserHandler -> validateToken : Token inválido");
                        return Mono.error(new UserValidationException(List.of(UserErrorCode.TOKEN_INVALID), List.of()));
                    }
                    logger.info("UserHandler -> validateToken : Token válido, Se envían claims");
                    return apiResponseBuilder.build(HttpStatus.OK, "Se envían claims", claims);
                });

    }


    public Mono<ServerResponse> getUsersMapEmails(ServerRequest req) {
        logger.info("UsuarioBatchHandler -> getUsersMapEmails : inicia el flujo.");
        return req.bodyToMono(LIST_STRING)
                .defaultIfEmpty(Set.of())
                .map(set -> set.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                )
                .flatMap((Set<String> emails) -> {
                    if (emails.isEmpty()) {
                        return apiResponseBuilder.build(HttpStatus.BAD_REQUEST,
                                "La lista de documentos no puede estar vacía", List.of());
                    }
                    logger.info("UsuarioBatchHandler -> getUsersMapEmails : {} documentos recibidos " + emails.size() + " ");

                    return userService.getUsersByEmails(emails)
                            .collectMap(User::getEmail, genericDtoMapper::toDto)
                            .flatMap(map -> apiResponseBuilder.build(
                                    HttpStatus.OK, "Usuarios recuperados correctamente", map));
                });
    }

}
