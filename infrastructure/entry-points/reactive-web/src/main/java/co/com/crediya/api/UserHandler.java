package co.com.crediya.api;


import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.login.TokenClaimsDto;
import co.com.crediya.api.dto.login.TokenDto;
import co.com.crediya.api.dto.user.EmailsRequestDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.api.response.UsersByEmailResponse;
import co.com.crediya.api.segurity.jwt.AuthenticationService;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.BadRequestException;
import co.com.crediya.model.exception.specific_exceptions.NotFoundException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.rol.gateways.RolService;
import co.com.crediya.usecase.user.gateways.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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


    private UserErrorCode mapMessageToErrorCode(String code) {
        return UserErrorCode.fromCode(code);
    }


    //primero
    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        logger.info("UserHandler -> login : inicia el flujo.");
        return serverRequest.bodyToMono(LoginDto.class)//se captura a dto
                .doOnNext(dto -> logger.info("UserHandler -> login : DTO recibido"))
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()//validan errores
                            .map(v -> mapMessageToErrorCode(v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    if (!infraErrors.isEmpty()) {
                        logger.warn("UserHandler -> login : Se encontraron errores de validación en el DTO");
                        return Mono.error(new UserValidationException(infraErrors, List.of()));
                    }
                    return Mono.just(dto);
                })
                .map(genericDtoMapper::toLogin)
                .doOnNext(login -> logger.info("UserHandler -> login : se mapea de dto a usuario"))
                .flatMap(userService::findIsExist)
                .flatMap(user -> rolService.findById(user.getRolId())
                        .map(role -> new TokenClaimsDto(//se crea dto para tomar la info para el token
                                user.getUserId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getDocumentId(),
                                user.getEmail(),
                                role.getName()
                        )))
                .flatMap(tokenClaimsDto -> {
                    TokenDto token = authenticationService.generateToken(tokenClaimsDto); //se retorna el token
                    return apiResponseBuilder.build(HttpStatus.OK, "Usuario logueado exitosamente", token.token());
                })
                .doOnSuccess(l -> logger.info("UserHandler -> login : Usuario autenticado exitosamente"));
    }
    // segundo AuthFilter
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
                            .doOnNext(u -> logger.info("UserHandler -> createUser : Usuario persistido"))
                            .map(genericDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("UserHandler -> createUser : Usuario creado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.CREATED, "Usuario creado exitosamente", userDto));
    }

    public Mono<ServerResponse> findByDocumentId(ServerRequest serverRequest) {
        logger.info("UserHandler -> findByDocumentId : inicia el flujo, consultar cliente con documento");
        return serverRequest.bodyToMono(UserDocumentDto.class)
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
                            .switchIfEmpty(Mono.error(new NotFoundException(UserErrorCode.USER_NOT_FOUND)))
                            .doOnNext(u -> logger.info("UserHandler -> findByDocumentId : Usuario encontrado"))
                            .map(genericDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("UserHandler -> findByDocumentId : Usuario consultado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.OK, "Usuario encontrado exitosamente", userDto));
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        logger.info("UserHandler -> findAll : inicia el flujo.");
        return userService.getAllUsers()
                .doOnNext(user -> logger.info("UserHandler -> findAll : se llama a userService.getAllUsers"))
                .doOnNext(user -> logger.info("UserHandler -> findAll : Se retornan todos los Usuarios"))
                .map(genericDtoMapper::toDto)
                .collectList()
                .flatMap(list -> {
                    logger.info("UserHandler -> findAll : Se recuperaron y transformaron todos los usuarios a DTO");
                    return apiResponseBuilder.build(HttpStatus.OK, "Usuarios recuperados exitosamente", list);
                })
                .doOnSuccess(resp -> logger.info("UserHandler -> findAll : Todos los usuarios fueron recuperados y enviados correctamente"));
    }



    //historia de solicitud
    public Mono<ServerResponse> getUsersMapEmails(ServerRequest serverRequest) {
        logger.info("UserHandler -> getUsersMapEmails : inicia el flujo.");

        return serverRequest.bodyToMono(EmailsRequestDto.class)
                .map(EmailsRequestDto::emails)
                .map(list -> list.stream()
                        .map(String::trim)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .filter(emails -> !emails.isEmpty())
                .switchIfEmpty(Mono.error(new BadRequestException(UserErrorCode.EMAIL_EMPTY)))
                .flatMap(emails -> {
                    logger.info("UserHandler -> getUsersMapEmails : " + emails.size() + " emails recibidos");

                    return userService.getUsersByEmails(emails)
                            .collectMap(User::getEmail, genericDtoMapper::toDto)
                            .flatMap(map -> {
                                UsersByEmailResponse resp = new UsersByEmailResponse(map);
                                return apiResponseBuilder.build(
                                        HttpStatus.OK,
                                        "Usuarios recuperados correctamente",
                                        resp
                                );
                            });
                });
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
                        return Mono.error(new UnauthorizedException(UserErrorCode.TOKEN_INVALID));
                    }
                    logger.info("UserHandler -> validateToken : Token válido, Se envían claims");
                    return apiResponseBuilder.build(HttpStatus.OK, "Se envían claims", claims);
                });

    }


}
