package co.com.crediya.api;

import co.com.crediya.api.dto.UserDto;
import co.com.crediya.api.exception.ApiResponseBuilder;
import co.com.crediya.api.logger.GlobalLogger;
import co.com.crediya.api.mapper.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserService;
import co.com.crediya.usecase.user.exception.UserValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserHandlerTest {
    @Mock
    private ApiResponseBuilder apiResponseBuilder;

    @Mock
    private UserService userService;

    @Mock
    private UserDtoMapper userDtoMapper;

    @Mock
    private Validator validator;
    private GlobalLogger logger;
    private UserHandler userHandler;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logger = Mockito.mock(GlobalLogger.class);
        userHandler = new UserHandler(apiResponseBuilder, userService, userDtoMapper, validator,logger);
    }

    @Test
    void testFindAllReturnsUsers() {

        UserDto userDto = new UserDto(
                "u01",
                "John Doe",
                "Gomez",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com",
                BigDecimal.valueOf(18000000.0));

        User user = new User("1",
                "John Doe",
                "Gomez",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com",
                BigDecimal.valueOf(18000000.0));


        when(userService.getAllUsers()).thenReturn(Flux.just(user));
        when(userDtoMapper.toDto(user)).thenReturn(userDto);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), any()))
                .thenAnswer(invocation -> {
                    Object body = invocation.getArgument(2);
                    return ServerResponse.status(HttpStatus.OK).bodyValue(body);
                });

        Mono<ServerResponse> responseMono = userHandler.findAll(MockServerRequest.builder().build());

        StepVerifier.create(responseMono)
                .assertNext(resp -> {
                    assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();

        verify(userService).getAllUsers();
        verify(userDtoMapper).toDto(user);
    }

    @Test
    void testFindAllHandlesError() {
        when(userService.getAllUsers()).thenReturn(Flux.error(new RuntimeException("DB error")));

        when(apiResponseBuilder.build(eq(HttpStatus.INTERNAL_SERVER_ERROR), anyString(), any()))
                .thenAnswer(invocation ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue(invocation.getArgument(2))
                );

        Mono<ServerResponse> responseMono = userHandler.findAll(MockServerRequest.builder().build());

        StepVerifier.create(responseMono)
                .assertNext(resp -> {
                    assertThat(resp.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .verifyComplete();

        verify(userService).getAllUsers();
        verify(apiResponseBuilder)
                .build(eq(HttpStatus.INTERNAL_SERVER_ERROR), eq("Error interno"), eq(List.of("Error al recuperar usuarios")));
    }

    @Test
    void testCreateUserSuccess() {
        UserDto userDto = new UserDto(
                "u01",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com",
                BigDecimal.valueOf(18000000.0)
        );

        User user = new User(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com",
                BigDecimal.valueOf(18000000.0)
        );

        when(validator.validate(any())).thenReturn(Set.of());

        when(userDtoMapper.toUser(userDto)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(Mono.just(user));
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        when(apiResponseBuilder.build(eq(HttpStatus.CREATED), anyString(), eq(userDto)))
                .thenAnswer(invocation ->
                        ServerResponse.status(HttpStatus.CREATED).bodyValue(invocation.getArgument(2))
                );

        Mono<ServerResponse> responseMono = userHandler.createUser(
                MockServerRequest.builder().body(Mono.just(userDto))
        );

        StepVerifier.create(responseMono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.CREATED))
                .verifyComplete();

        verify(userService).createUser(user);
        verify(userDtoMapper).toUser(userDto);
        verify(userDtoMapper).toDto(user);
    }

    @Test
    void testCreateUserValidationFails() {
        UserDto userDto = new UserDto(
                "u01",
                null,
                "",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "invalid-email",
                BigDecimal.ONE
        );

        @SuppressWarnings("unchecked")
        ConstraintViolation<UserDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("USR_001");
        when(validator.<UserDto>validate(any(UserDto.class)))
                .thenReturn(Collections.singleton(violation));

        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/users"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(userDto));

        Mono<ServerResponse> responseMono = userHandler.createUser(request);

        StepVerifier.create(responseMono)
                .expectError(UserValidationException.class)
                .verify();

        verify(validator).validate(any(UserDto.class));
        verifyNoInteractions(userService);
    }
}