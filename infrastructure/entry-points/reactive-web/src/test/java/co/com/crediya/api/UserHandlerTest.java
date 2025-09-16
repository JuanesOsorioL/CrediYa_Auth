package co.com.crediya.api;

import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.segurity.TokenDto;
import co.com.crediya.api.dto.user.EmailsRequestDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.response.ApiResponseBuilder;
import co.com.crediya.api.response.UsersByEmailResponse;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.BadRequestException;
import co.com.crediya.model.exception.specific_exceptions.NotFoundException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.segurity.SegurityGateway;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.rol.gateways.RolService;
import co.com.crediya.usecase.user.gateways.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserHandlerTest {

    private ApiResponseBuilder apiResponseBuilder;
    private UserService userService;
    private RolService rolService;
    private GenericDtoMapper genericDtoMapper;
    private Validator validator;
    private Logger logger;
    private SegurityGateway segurityGateway;

    private UserHandler userHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiResponseBuilder = mock(ApiResponseBuilder.class);
        userService = mock(UserService.class);
        rolService = mock(RolService.class);
        genericDtoMapper = mock(GenericDtoMapper.class);
        validator = mock(Validator.class);
        logger = mock(Logger.class);
        segurityGateway = mock(SegurityGateway.class);

        userHandler = new UserHandler(
                apiResponseBuilder, userService, rolService, genericDtoMapper, validator, logger, segurityGateway
        );
    }

    @Test
    void testFindAllReturnsUsers() {
        var user = new co.com.crediya.model.user.User(
                "1", "John", "Gomez", "123456789",
                LocalDate.of(1990, 1, 1), "123456789", "john@doe.com", "123", "Customer",
                BigDecimal.valueOf(18_000_000.0)
        );
        var dto = new UserDto(
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
        var userDto = new UserDto(
                "u01", "John", "Doe", LocalDate.of(1990, 1, 1),
                "123456789", "john@doe.com", "123456789", "123", "Customer",
                BigDecimal.valueOf(18_000_000.0)
        );

        var user = new co.com.crediya.model.user.User(
                "1", "John", "Doe", "123456789", LocalDate.of(1990, 1, 1),
                "123456789", "john@doe.com", "123", "Customer", BigDecimal.valueOf(18_000_000.0)
        );

        when(validator.validate(any())).thenReturn(Set.of());
        when(genericDtoMapper.toUser(userDto)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(Mono.just(user));
        when(genericDtoMapper.toDto(user)).thenReturn(userDto);
        when(apiResponseBuilder.build(eq(HttpStatus.CREATED), anyString(), eq(userDto)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.CREATED).bodyValue(inv.getArgument(2)));

        Mono<ServerResponse> responseMono =
                userHandler.createUser(MockServerRequest.builder().body(Mono.just(userDto)));

        StepVerifier.create(responseMono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.CREATED))
                .verifyComplete();

        verify(userService).createUser(user);
        verify(genericDtoMapper).toUser(userDto);
        verify(genericDtoMapper).toDto(user);
    }

    @Test
    void testCreateUserValidationFails() {
        var userDto = new UserDto(
                "u01", null, "", LocalDate.of(1990, 1, 1),
                "123456789", "invalid-email", "123456789", "123", "Customer", BigDecimal.ONE
        );

        @SuppressWarnings("unchecked")
        ConstraintViolation<UserDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("USR_001");
        when(validator.<UserDto>validate(any(UserDto.class))).thenReturn(Set.of(violation));

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
        var docDto = new UserDocumentDto("DOC-123456");
        var user = new co.com.crediya.model.user.User(
                "1", "John", "Doe", "DOC-123456",
                LocalDate.of(1990, 1, 1), "3000000000", "john@doe.com", "123", "Customer",
                BigDecimal.valueOf(1_000_000)
        );
        var userDto = new UserDto(
                "u01", "John", "Doe", LocalDate.of(1990, 1, 1),
                "DOC-123456", "john@doe.com", "3000000000", "123", "Customer", BigDecimal.valueOf(1_000_000)
        );

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
        var docDto = new UserDocumentDto("DOC-BAD");

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
        var docDto = new UserDocumentDto("DOC-404");
        var temp = new co.com.crediya.model.user.User(
                "x", "A", "B", "DOC-404", LocalDate.now(), "3", "a@b.com", "p", "Customer", BigDecimal.TEN
        );

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

    @Test
    void emptyOrNullEmails_throwsBadRequest() {
        // caso emails == null
        EmailsRequestDto bodyNull = new EmailsRequestDto(null);
        ServerRequest reqNull = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(bodyNull));

        StepVerifier.create(userHandler.getUsersMapEmails(reqNull))
                .expectError(BadRequestException.class)
                .verify();

        // caso solo espacios/blancos
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
    void trimsAndDedups_thenBuildsResponse_ok() {
        EmailsRequestDto body = new EmailsRequestDto(Arrays.asList(" a@ex.com ", "", "b@ex.com", "a@ex.com", "  "));

        User u1 = new User("1", "A", "A", "D1", LocalDate.now(), "3", "a@ex.com", "p", "Customer", BigDecimal.ONE);
        User u2 = new User("2", "B", "B", "D2", LocalDate.now(), "4", "b@ex.com", "p", "Customer", BigDecimal.TEN);

        UserDto d1 = new UserDto("1", "A", "A", LocalDate.now(), "D1", "a@ex.com", "3", "p", "Customer", BigDecimal.ONE);
        UserDto d2 = new UserDto("2", "B", "B", LocalDate.now(), "D2", "b@ex.com", "4", "p", "Customer", BigDecimal.TEN);

        when(userService.getUsersByEmails(argThat(set ->
                set.size() == 2 && set.contains("a@ex.com") && set.contains("b@ex.com"))))
                .thenReturn(Flux.just(u1, u2));
        when(genericDtoMapper.toDto(u1)).thenReturn(d1);
        when(genericDtoMapper.toDto(u2)).thenReturn(d2);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), any(UsersByEmailResponse.class)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        StepVerifier.create(userHandler.getUsersMapEmails(req))
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        // Capturar payload para validar el mapa
        ArgumentCaptor<UsersByEmailResponse> cap = ArgumentCaptor.forClass(UsersByEmailResponse.class);
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), cap.capture());

        UsersByEmailResponse payload = cap.getValue();
        assertThat(payload).isNotNull();

        // reflexión sencilla para leer el Map interno 'usersByEmail' si es private
        var fields = payload.getClass().getDeclaredFields();
        assertThat(fields).isNotEmpty();
        fields[0].setAccessible(true);
        Object inner;
        try {
            inner = fields[0].get(payload);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        assertThat(inner).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, ?> outMap = (Map<String, ?>) inner;
        assertThat(outMap).hasSize(2).containsKeys("a@ex.com", "b@ex.com");
    }

    @Test
    void nullItemsInsideList_areIgnored() {
        EmailsRequestDto body = new EmailsRequestDto(Arrays.asList(null, "  a@ex.com ", null, "b@ex.com", "  "));

        User u1 = new User("1", "A", "A", "D1", LocalDate.now(), "3", "a@ex.com", "p", "Customer", BigDecimal.ONE);
        User u2 = new User("2", "B", "B", "D2", LocalDate.now(), "4", "b@ex.com", "p", "Customer", BigDecimal.TEN);

        UserDto d1 = new UserDto("1", "A", "A", LocalDate.now(), "D1", "a@ex.com", "3", "p", "Customer", BigDecimal.ONE);
        UserDto d2 = new UserDto("2", "B", "B", LocalDate.now(), "D2", "b@ex.com", "4", "p", "Customer", BigDecimal.TEN);

        when(userService.getUsersByEmails(argThat(set ->
                set.size() == 2 && set.contains("a@ex.com") && set.contains("b@ex.com"))))
                .thenReturn(Flux.just(u1, u2));
        when(genericDtoMapper.toDto(u1)).thenReturn(d1);
        when(genericDtoMapper.toDto(u2)).thenReturn(d2);
        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), any(UsersByEmailResponse.class)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        StepVerifier.create(userHandler.getUsersMapEmails(req))
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(userService).getUsersByEmails(argThat(set -> set.size() == 2));
    }

    @Test
    void serviceError_isPropagated() {
        EmailsRequestDto body = new EmailsRequestDto(List.of("a@ex.com"));

        when(userService.getUsersByEmails(anySet())).thenReturn(Flux.error(new RuntimeException("DB down")));

        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.POST).uri(URI.create("/usuarios/mapa"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(body));

        StepVerifier.create(userHandler.getUsersMapEmails(req))
                .expectErrorMatches(t -> t instanceof RuntimeException && "DB down".equals(t.getMessage()))
                .verify();

        verifyNoInteractions(apiResponseBuilder);
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
        verifyNoInteractions(segurityGateway);
    }

    @Test
    void testValidateToken_Invalid_returnsUnauthorizedException() {
        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .header("Authorization", "Bearer badtoken")
                .build();

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("badtoken"));
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(null);

        Mono<ServerResponse> mono = userHandler.validateToken(req);

        StepVerifier.create(mono)
                .expectError(UnauthorizedException.class)
                .verify();

        verify(segurityGateway).validateTokenClaims(any(Token.class));
        verifyNoInteractions(apiResponseBuilder);
    }

    @Test
    void testValidateToken_Valid_returns200() {
        ServerRequest req = MockServerRequest.builder()
                .method(HttpMethod.GET).uri(URI.create("/validateToken"))
                .header("Authorization", "Bearer goodtoken")
                .build();

        when(genericDtoMapper.toToken(any(TokenDto.class))).thenReturn(new Token("goodtoken"));
        var domainClaims = new Claismo("Ana", "ana@ex.com", "Admin", "1700000000", "Gómez", "123", "1690000000", "user-1");
        when(segurityGateway.validateTokenClaims(any(Token.class))).thenReturn(domainClaims);
        var claimsDto = new co.com.crediya.api.dto.segurity.ClaismoDto("Ana", "ana@ex.com", "Admin", "1700000000", "Gómez", "123", "1690000000", "user-1");
        when(genericDtoMapper.toClaismoDto(domainClaims)).thenReturn(claimsDto);

        when(apiResponseBuilder.build(eq(HttpStatus.OK), anyString(), eq(claimsDto)))
                .thenAnswer(inv -> ServerResponse.status(HttpStatus.OK).bodyValue(inv.getArgument(2)));

        Mono<ServerResponse> mono = userHandler.validateToken(req);

        StepVerifier.create(mono)
                .assertNext(resp -> assertThat(resp.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(segurityGateway).validateTokenClaims(any(Token.class));
        verify(genericDtoMapper).toClaismoDto(domainClaims);
        verify(apiResponseBuilder).build(eq(HttpStatus.OK), anyString(), eq(claimsDto));
    }

    @Test
    void testLoginValidationFails() {
        var body = new LoginDto("bad-email", "x");

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
        verifyNoInteractions(userService, rolService, segurityGateway, apiResponseBuilder);
    }

    @Test
    void testLoginUnauthorizedFromUserService() {
        var body = new LoginDto("john@doe.com", "wrong");

        when(validator.<LoginDto>validate(any(LoginDto.class))).thenReturn(Set.of());

        var domainLogin = new Login("john@doe.com", "wrong");
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
        var body = new LoginDto("john@doe.com", "pass");

        when(validator.<LoginDto>validate(any(LoginDto.class))).thenReturn(Set.of());

        var login = new Login("john@doe.com", "pass");
        when(genericDtoMapper.toLogin(body)).thenReturn(login);

        var user = new co.com.crediya.model.user.User(
                "u1", "John", "Doe", "DOC-1",
                LocalDate.of(1990, 1, 1), "300", "john@doe.com", "pass",
                "ROL_ID", BigDecimal.valueOf(1_000_000)
        );
        when(userService.findIsExist(any())).thenReturn(Mono.just(user));

        var rol = mock(co.com.crediya.model.rol.Rol.class);
        when(rol.getName()).thenReturn("Admin");
        when(rolService.findById("ROL_ID")).thenReturn(Mono.just(rol));

        when(segurityGateway.generateToken(any())).thenReturn(new Token("tok-123"));
        when(genericDtoMapper.toTokenDto(new Token("tok-123"))).thenReturn(new TokenDto("tok-123"));

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
        verify(segurityGateway).generateToken(any());
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
        verifyNoInteractions(segurityGateway);
    }
}

