package co.com.crediya.api;


import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.user.EmailsRequestDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.api.response.UsersByEmailResponse;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.BadRequestException;
import co.com.crediya.model.exception.specific_exceptions.NotFoundException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.user.gateways.UserService;
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

    public static final String VERIFICAN_LOS_ERRORES_DE_JAKARTA_DEL_REQUEST = "UserHandler -> validarInfra : se verifican los errores de jakarta del request";
    public static final String SE_ENCONTRARON_ERRORES_DE_VALIDACION_EN_EL_DTO = "UserHandler -> validarInfra : Se encontraron errores de validación en el DTO";
    public static final String DTO_RECIBIDO = "UserHandler -> login : DTO recibido";
    public static final String NO_SE_ENCONTRARON_ERRORES_JAKARTA_LOGIN = "UserHandler -> login : No se encontraron Errores jakarta";
    public static final String NO_SE_ENCONTRARON_ERRORES_JAKARTA_CREATE_USER = "UserHandler -> createUser : No se encontraron Errores jakarta";
    public static final String MAPEA_DE_DTO_A_USUARIO = "UserHandler -> login : se mapea de dto a usuario";
    public static final String AUTENTICADO_EXITOSAMENTE = "Usuario autenticado exitosamente";
    public static final String USUARIO_AUTENTICADO_EXITOSAMENTE = "UserHandler -> login : Usuario autenticado exitosamente";
    public static final String LOGIN_DTO_PARA_LA_RESPUESTA = "UserHandler -> login : Se mapea a loginDto para la respuesta";
    public static final String USER_DTO_RECIBIDO = "UserHandler -> createUser : DTO recibido";
    public static final String SE_MAPEA_A_DOMINIO = "UserHandler -> createUser : Se mapea a dominio";

    private final ApiResponseBuilder apiResponseBuilder;
    private final UserService userService;
    private final GenericDtoMapper genericDtoMapper;
    private final Validator validator;
    private final Logger logger;
    private final SegurityGateway segurityGateway;


    private UserErrorCode mapMessageToErrorCode(String code) {
        return UserErrorCode.fromCode(code);
    }

    private <T> Mono<T> validarInfra(T dto) {
        logger.info(VERIFICAN_LOS_ERRORES_DE_JAKARTA_DEL_REQUEST);
        return Mono.defer(() -> {
            var infraErrors = validator.validate(dto).stream()//validan errores
                    .map(v -> mapMessageToErrorCode(v.getMessage()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (!infraErrors.isEmpty()) {
                logger.error(SE_ENCONTRARON_ERRORES_DE_VALIDACION_EN_EL_DTO);
                return Mono.error(new UserValidationException(infraErrors, List.of()));
            }
            return Mono.just(dto);
        });
    }

    //primero
    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        logger.info("UserHandler -> login : inicia el flujo.");
        return serverRequest.bodyToMono(LoginDto.class)//se captura a dto
                .doOnNext(dto -> logger.info(DTO_RECIBIDO))
                .flatMap(this::validarInfra)
                .doOnNext(sub -> logger.info(NO_SE_ENCONTRARON_ERRORES_JAKARTA_LOGIN))
                .map(genericDtoMapper::toLogin)
                .doOnNext(login -> logger.info(MAPEA_DE_DTO_A_USUARIO))
                .flatMap(userService::findIsExist)
                .flatMap(segurityGateway::generateToken)
                .map(genericDtoMapper::toTokenDto)
                .doOnNext(login -> logger.info(LOGIN_DTO_PARA_LA_RESPUESTA))
                .flatMap(tokenDto -> apiResponseBuilder.build(HttpStatus.OK, AUTENTICADO_EXITOSAMENTE, tokenDto))
                .doOnSuccess(l -> logger.info(USUARIO_AUTENTICADO_EXITOSAMENTE));
    }

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        logger.info("UserHandler -> createUser : inicia el flujo.");
        return serverRequest.bodyToMono(UserDto.class)
                .doOnNext(dto -> logger.info(USER_DTO_RECIBIDO))
                .flatMap(this::validarInfra)
                .doOnNext(sub -> logger.info(NO_SE_ENCONTRARON_ERRORES_JAKARTA_CREATE_USER))
                .map(genericDtoMapper::toUser)
                .doOnNext(sub -> logger.info(SE_MAPEA_A_DOMINIO))
                .flatMap(userService::createUser)
                .doOnNext(u -> logger.info("UserHandler -> createUser : Usuario persistido"))
                .map(genericDtoMapper::toDto)
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.CREATED, "Usuario creado exitosamente", userDto))
                .doOnSuccess(dto -> logger.info("UserHandler -> createUser : Usuario creado exitosamente"));
    }

    //historia de solicitud
    public Mono<ServerResponse> getUsersMapEmails(ServerRequest serverRequest) {
        logger.info("UserHandler -> getUsersMapEmails : inicia el flujo.");

        return serverRequest.bodyToMono(EmailsRequestDto.class)
                // si viene null, usar lista vacía
                .map(dto -> java.util.Optional.ofNullable(dto.emails())
                        .orElse(java.util.Collections.emptyList()))
                // trim, quitar nulos y vacíos, y deduplicar preservando orden
                .map(list -> list.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)))
                // si quedó vacío => 400
                .filter(emails -> !emails.isEmpty())
                .switchIfEmpty(Mono.error(new BadRequestException(UserErrorCode.EMAIL_EMPTY)))
                // buscar y responder
                .flatMap(emails -> {
                    logger.info("UserHandler -> getUsersMapEmails : " + emails.size() + " emails recibidos");
                    return userService.getUsersByEmails(emails)
                            .collectMap(User::getEmail, genericDtoMapper::toDto)
                            .flatMap(map -> apiResponseBuilder.build(
                                    HttpStatus.OK,
                                    "Usuarios recuperados correctamente",
                                    new UsersByEmailResponse(map)
                            ));
                });
    }

    public Mono<ServerResponse> validateToken(ServerRequest serverRequest) {
        logger.info("UserHandler -> validateToken : inicia el flujo.");
        Claismo claismo = serverRequest.exchange().getAttribute("claims");
        return apiResponseBuilder.build(HttpStatus.OK, "Se envían ClaismoDto", genericDtoMapper.toClaismoDto(claismo))
                .doOnSuccess(dto -> logger.info("UserHandler -> validateToken : Se envían ClaismoDto"));
    }

    //
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
                            .doOnNext(sub -> logger.info("UserHandler -> findByDocumentId : Invocando UserService.findByDocumentId"))
                            .switchIfEmpty(Mono.error(new NotFoundException(UserErrorCode.USER_NOT_FOUND)))
                            .doOnNext(u -> logger.info("UserHandler -> findByDocumentId : Usuario encontrado"))
                            .map(genericDtoMapper::toDto);
                })
                .doOnSuccess(dto -> logger.info("UserHandler -> findByDocumentId : Usuario consultado exitosamente"))
                .flatMap(userDto -> apiResponseBuilder.build(HttpStatus.OK, "Usuario encontrado exitosamente", userDto));
    }

    //si funciona no estan en los criterios
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

}
