package co.com.crediya.api;

import co.com.crediya.api.dto.login.LoginDto;
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
import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.rol.gateways.RolService;
import co.com.crediya.usecase.user.gateways.UserService;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserHandlerTest {

    private ApiResponseBuilder apiResponseBuilder;
    private UserService userService;
    private RolService rolService;
    private GenericDtoMapper genericDtoMapper;
    private Validator validator;
    private GlobalLogger logger;
    private AuthenticationService authenticationService;

    private UserHandler userHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiResponseBuilder = mock(ApiResponseBuilder.class);
        userService = mock(UserService.class);
        rolService = mock(RolService.class);
        genericDtoMapper = mock(GenericDtoMapper.class);
        validator = mock(Validator.class);
        logger = mock(GlobalLogger.class);
        authenticationService = mock(AuthenticationService.class);

        userHandler = new UserHandler(apiResponseBuilder, userService, rolService, genericDtoMapper, validator, logger, authenticationService);
    }

    @Test
    void testFindAllReturnsUsers() {
        User user = new User(
                "1", "John", "Gomez", "123456789",
                LocalDate.of(1990, 1, 1), "123456789", "john@doe.com", "123", "Customer",
                BigDecimal.valueOf(18_000_000.0)
        );
        UserDto dto = new UserDto(
                "u01", "John", "Gomez", LocalDate.of(1990, 1, 1),
                "123456789", "john@doe.com", "123456789", "123", "Customer",
                BigDecimal.valueOf(18_000_000.0)
        );


        when(userService.getAllUsers()).thenReturn(Flux.just(user));
        when(genericDtoMapper.toDto(user)).thenReturn(dto);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), any()))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        Mono<ServerResponse> mono = userHandler.findAll(MockServerRequest.builder().build());

        StepVerifier.create(mono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(userService).getAllUsers();
        verify(genericDtoMapper).toDto(user);
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), any());
    }

    @Test
    void testFindAllHandlesError() {
        when(userService.getAllUsers()).thenReturn(Flux.error(new RuntimeException("DB error")));

        Mono<ServerResponse> mono = userHandler.findAll(MockServerRequest.builder().build());

        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof RuntimeException && "DB error".equals(t.getMessage()))
                .verify();

        verify(userService).getAllUsers();
        verifyNoInteractions(apiResponseBuilder);
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
                "123456789", "123", "Customer",
                BigDecimal.valueOf(18000000.0)
        );

        User user = new User(
                "1",
                "John",
                "Doe",
                "123456789",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "john@doe.com", "123", "Customer",
                BigDecimal.valueOf(18000000.0)
        );

        when(validator.validate(any())).thenReturn(Set.of());

        when(genericDtoMapper.toUser(userDto)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(Mono.just(user));
        when(genericDtoMapper.toDto(user)).thenReturn(userDto);

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
        verify(genericDtoMapper).toUser(userDto);
        verify(genericDtoMapper).toDto(user);
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
                "123456789", "123", "Customer",
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

    @Test
    void testFindByDocumentIdSuccess() {
        UserDocumentDto docDto = new UserDocumentDto("DOC-123456");

        User user = new User("1", "John", "Doe", "DOC-123456",
                LocalDate.of(1990, 1, 1), "3000000000", "john@doe.com", "123", "Customer", BigDecimal.valueOf(1_000_000));

        UserDto userDto = new UserDto("u01", "John", "Doe",
                LocalDate.of(1990, 1, 1), "DOC-123456", "john@doe.com", "3000000000", "123", "Customer", BigDecimal.valueOf(1_000_000));


        when(validator.validate(any(UserDocumentDto.class))).thenReturn(Set.of());
        when(genericDtoMapper.toUser(any(UserDocumentDto.class))).thenReturn(user);
        when(userService.findByDocumentId("DOC-123456")).thenReturn(Mono.just(user));
        when(genericDtoMapper.toDto(user)).thenReturn(userDto);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), eq(userDto)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/usuarios/documento"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(docDto));

        Mono<ServerResponse> resp = userHandler.findByDocumentId(request);

        StepVerifier.create(resp)
                .assertNext(sr -> assertThat(sr.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(validator).validate(any(UserDocumentDto.class));
        verify(genericDtoMapper).toUser(any(UserDocumentDto.class));
        verify(userService).findByDocumentId("DOC-123456");
        verify(genericDtoMapper).toDto(user);
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), eq(userDto));
    }

    @Test
    void testFindByDocumentIdInfraValidationFails() {
        UserDocumentDto docDto = new UserDocumentDto("DOC-BAD");

        @SuppressWarnings("unchecked")
        ConstraintViolation<UserDocumentDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("USR_001");
        when(validator.<UserDocumentDto>validate(any(UserDocumentDto.class)))
                .thenReturn(Collections.singleton(violation));

        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/usuarios/documento"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(docDto));

        Mono<ServerResponse> resp = userHandler.findByDocumentId(request);

        StepVerifier.create(resp)
                .expectError(UserValidationException.class)
                .verify();

        verify(validator).validate(any(UserDocumentDto.class));
        verifyNoInteractions(userService);
        verifyNoMoreInteractions(apiResponseBuilder);
    }


    @Test
    void testFindByDocumentIdNotFound() {
        UserDocumentDto docDto = new UserDocumentDto("DOC-404");
        User temp = new User("x", "A", "B", "DOC-404", LocalDate.now(),
                "3", "a@b.com", "p", "Customer", BigDecimal.TEN);

        when(validator.validate(any(UserDocumentDto.class))).thenReturn(Set.of());
        when(genericDtoMapper.toUser(any(UserDocumentDto.class))).thenReturn(temp);
        when(userService.findByDocumentId("DOC-404")).thenReturn(Mono.empty());

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/documento"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(docDto));

        Mono<ServerResponse> mono = userHandler.findByDocumentId(req);

        StepVerifier.create(mono)
                .expectError(NotFoundException.class)
                .verify();

        verify(userService).findByDocumentId("DOC-404");
        verifyNoInteractions(apiResponseBuilder);
    }

    @SneakyThrows
    @Test
    void testGetUsersMapEmailsSuccess_trimAndDedup() {
        EmailsRequestDto body = new EmailsRequestDto(Arrays.asList(
                " a@ex.com ", "", "b@ex.com", "a@ex.com", "  "
        ));

        User u1 = new User("1", "A", "A", "D1", LocalDate.now(), "3", "a@ex.com", "p", "Customer", BigDecimal.ONE);
        User u2 = new User("2", "B", "B", "D2", LocalDate.now(), "4", "b@ex.com", "p", "Customer", BigDecimal.TEN);

        UserDto d1 = new UserDto("1", "A", "A", LocalDate.now(), "D1", "a@ex.com", "3", "p", "Customer", BigDecimal.ONE);
        UserDto d2 = new UserDto("2", "B", "B", LocalDate.now(), "D2", "b@ex.com", "4", "p", "Customer", BigDecimal.TEN);

        when(userService.getUsersByEmails(argThat(set -> set.size() == 2 && set.contains("a@ex.com") && set.contains("b@ex.com"))))
                .thenReturn(Flux.just(u1, u2));

        when(genericDtoMapper.toDto(u1)).thenReturn(d1);
        when(genericDtoMapper.toDto(u2)).thenReturn(d2);

        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), any(UsersByEmailResponse.class)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        Mono<ServerResponse> mono = userHandler.getUsersMapEmails(req);

        StepVerifier.create(mono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();


        ArgumentCaptor<Object> bodyCap = ArgumentCaptor.forClass(Object.class);
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), bodyCap.capture());

        Object payload = bodyCap.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.getClass().getSimpleName()).isEqualTo("UsersByEmailResponse");


        var fields = payload.getClass().getDeclaredFields();
        assertThat(fields).isNotEmpty();
        fields[0].setAccessible(true);
        Object inner = fields[0].get(payload);
        assertThat(inner).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, ?> outMap = (Map<String, ?>) inner;
        assertThat(outMap).hasSize(2).containsKeys("a@ex.com", "b@ex.com");
    }

    @Test
    void testGetUsersMapEmailsEmptyOrNull_throwsBadRequest() {
        EmailsRequestDto bodyNull = new EmailsRequestDto(null);

        ServerRequest reqNull = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(bodyNull));

        StepVerifier.create(userHandler.getUsersMapEmails(reqNull))
                .expectError(BadRequestException.class)
                .verify();

        EmailsRequestDto bodyEmpty = new EmailsRequestDto(Arrays.asList("  ", "", "   "));
        ServerRequest reqEmpty = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(bodyEmpty));

        StepVerifier.create(userHandler.getUsersMapEmails(reqEmpty))
                .expectError(BadRequestException.class)
                .verify();

        verifyNoInteractions(userService, genericDtoMapper, apiResponseBuilder);
    }

    @Test
    void testValidateToken_NoHeader_returns401() {
        when(apiResponseBuilder.build(eq(HttpStatus.UNAUTHORIZED), anyString(), isNull()))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.UNAUTHORIZED).build());

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .build();

        Mono<ServerResponse> mono = userHandler.validateToken(req);

        StepVerifier.create(mono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .verifyComplete();

        verify(apiResponseBuilder).build(eq(HttpStatus.UNAUTHORIZED), anyString(), isNull());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void testValidateToken_Invalid_returnsUnauthorizedException() {
        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .header("Authorization", "Bearer badtoken")
                .build();

        when(authenticationService.validateTokenAndGetClaims(any())).thenReturn(null);

        Mono<ServerResponse> mono = userHandler.validateToken(req);

        StepVerifier.create(mono)
                .expectError(UnauthorizedException.class)
                .verify();

        verify(authenticationService).validateTokenAndGetClaims(any());
        verifyNoInteractions(apiResponseBuilder);
    }

    @Test
    void testValidateToken_Valid_returns200() {
        Claims claims = mock(Claims.class);
        when(authenticationService.validateTokenAndGetClaims(any())).thenReturn(claims);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), eq(claims)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .header("Authorization", "Bearer goodtoken")
                .build();

        Mono<ServerResponse> mono = userHandler.validateToken(req);

        StepVerifier.create(mono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(authenticationService).validateTokenAndGetClaims(any());
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), eq(claims));
    }

    @Test
    void testLoginValidationFails() {
        LoginDto body = new LoginDto("bad-email", "x");

        @SuppressWarnings("unchecked")
        ConstraintViolation<LoginDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("USR_001");
        when(validator.<LoginDto>validate(any(LoginDto.class))).thenReturn(Set.of(violation));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/api/v1/login"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        Mono<ServerResponse> mono = userHandler.login(req);

        StepVerifier.create(mono)
                .expectError(UserValidationException.class)
                .verify();

        verify(validator).validate(any(LoginDto.class));
        verifyNoInteractions(userService, rolService, authenticationService, apiResponseBuilder);
    }

    @Test
    void testLoginUnauthorizedFromUserService() {
        LoginDto body = new LoginDto("john@doe.com", "wrong");


        when(validator.<LoginDto>validate(any(LoginDto.class))).thenReturn(Set.of());

        Login domainLogin = new Login("john@doe.com", "wrong");


        when(genericDtoMapper.toLogin(body)).thenReturn(domainLogin);

        when(userService.findIsExist(any())).thenReturn(Mono.error(new UnauthorizedException(UserErrorCode.LOGUIN_FAIL_USER_NOT_FOUND)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/api/v1/login"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        Mono<ServerResponse> mono = userHandler.login(req);

        StepVerifier.create(mono)
                .expectError(UnauthorizedException.class)
                .verify();

        verify(genericDtoMapper).toLogin(body);
        verify(userService).findIsExist(any());
        verifyNoInteractions(apiResponseBuilder);
    }



    @Test
    void testLoginSuccess_returns200AndToken() {
        LoginDto body = new LoginDto("john@doe.com", "pass");

        when(validator.<LoginDto>validate(any(LoginDto.class))).thenReturn(Set.of());

        Login login = new Login("john@doe.com", "pass");
        when(genericDtoMapper.toLogin(body)).thenReturn(login);

        User user = new User(
                "u1", "John", "Doe", "DOC-1",
                LocalDate.of(1990,1,1), "300", "john@doe.com", "pass",
                "ROL_ID", BigDecimal.valueOf(1_000_000)
        );
        when(userService.findIsExist(any())).thenReturn(Mono.just(user));

        var rol = mock(co.com.crediya.model.rol.Rol.class);
        when(rol.getName()).thenReturn("Admin");
        when(rolService.findById("ROL_ID")).thenReturn(Mono.just(rol));

        when(authenticationService.generateToken(any())).thenReturn(new TokenDto("tok-123"));
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), eq("tok-123")))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/api/v1/login"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        StepVerifier.create(userHandler.login(req))
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(rolService).findById("ROL_ID");
        verify(authenticationService).generateToken(any());
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), eq("tok-123"));
    }

    @Test
    void testValidateToken_BlankHeader_returns401() {

        when(apiResponseBuilder.build(eq(HttpStatus.UNAUTHORIZED), anyString(), isNull()))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.UNAUTHORIZED).build());

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .header("Authorization", "   ")
                .build();

        StepVerifier.create(userHandler.validateToken(req))
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .verifyComplete();

        verify(apiResponseBuilder).build(eq(HttpStatus.UNAUTHORIZED), anyString(), isNull());
        verifyNoInteractions(authenticationService);
    }
}