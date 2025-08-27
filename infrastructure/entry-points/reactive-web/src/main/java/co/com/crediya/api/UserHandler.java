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
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final Validator validator;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserHandler.class);

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .flatMap(dto -> {
                    List<UserErrorCode> infraErrors = validator.validate(dto).stream()
                            .map(v -> mapConstraintViolationToErrorCode(v.getPropertyPath().toString(), v.getMessage()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    User user = userDtoMapper.toUser(dto);
                    log.info("errores de infraestructura: {}", infraErrors);
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
                        ApiResponse<List<String>> errorResponse = new ApiResponse<>();
                        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                        errorResponse.setMessage("Errores de validación");
                        errorResponse.setBody(allErrors.stream()
                                .map(UserErrorCode::getMessage)
                                .toList());
                        return ServerResponse.badRequest().bodyValue(errorResponse);
                    }

                    ApiResponse<UserDto> successResponse = new ApiResponse<>();
                    successResponse.setStatus(HttpStatus.CREATED.value());
                    successResponse.setMessage("Usuario creado exitosamente");
                    successResponse.setBody(userDtoMapper.toDto(result.getUser()));
                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(successResponse);
                })
                .onErrorResume(e -> {
                    if (e instanceof ValidationException ve) {
                        return errorResponse(HttpStatus.BAD_REQUEST, "Errores en la entrada", ve.getErrors());
                    } else if (e instanceof DomainValidationException dve) {
                        return errorResponse(HttpStatus.BAD_REQUEST, "Errores de negocio", dve.getErrors());
                    } else {
                        log.error("Error inesperado", e);
                        return
                                errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", List.of(UserErrorCode.GENERIC_ERROR));
                    }
                });
    }

    private Mono<ServerResponse> errorResponse(HttpStatus status, String message, List<UserErrorCode> errors) {
        ApiResponse<List<String>> response = new ApiResponse<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setBody(errors.stream().map(UserErrorCode::getMessage).toList());
        return ServerResponse.status(status).bodyValue(response);
    }

    private UserErrorCode mapConstraintViolationToErrorCode(String propertyPath, String message) {
        return switch (propertyPath) {
            case "firstName" -> UserErrorCode.FIRST_NAME_EMPTY;
            case "lastName" -> UserErrorCode.LAST_NAME_EMPTY;
            case "email" -> {
                if (message.contains("El correo electrónico no puede ser vacío")) yield UserErrorCode.EMAIL_EMPTY;
                else yield UserErrorCode.EMAIL_INVALID;
            }
            case "baseSalary" -> {
                if (message.contains("no puede ser vacío")) yield UserErrorCode.BASE_SALARY_EMPTY;
                else yield UserErrorCode.BASE_SALARY_INVALID;
            }
            default -> null;
        };
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
