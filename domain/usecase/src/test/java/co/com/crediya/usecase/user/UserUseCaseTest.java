package co.com.crediya.usecase.user;

import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.ConflictException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.exception.UserValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.*;

class UserUseCaseTest {

    UserRepository userRepository;
    Logger logger;
    UserUseCase userUseCase;
    LocalDate fechaHoy = LocalDate.now();

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        logger = Mockito.mock(Logger.class);
        userUseCase = new UserUseCase(userRepository, logger);

        when(userRepository.existUserByDocumentId(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.findAll()).thenReturn(Flux.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }

    @Test
    void createUser_ShouldFail_WhenEmailAlreadyExists_conflict() {
        User user = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3002223333", "john@example.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        when(userRepository.existUserByDocumentId("DOC-123456")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.createUser(user))
                .expectError(ConflictException.class)
                .verify();

        verify(userRepository).existUserByDocumentId("DOC-123456");
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenDocumentAlreadyExists_conflict() {
        User user = new User("1", "juan", "carlos",
                "DOC-999", fechaHoy, "3001112222", "john@example.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        when(userRepository.existUserByDocumentId("DOC-999")).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.createUser(user))
                .expectError(ConflictException.class)
                .verify();

        verify(userRepository).existUserByDocumentId("DOC-999");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenFirstNameLastNameAndEmailAreEmpty() {
        User invalidUser = new User("1", "", "",
                "DOC-123456", fechaHoy, "3000000000", "", "1234", "Customer", BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.FIRST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.LAST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_EMPTY)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryIsInvalid() {
        User invalidUser = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3000000000", "a@b.com", "1234",
                "Customer", BigDecimal.valueOf(-1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_INVALID)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenFirstNameLastNameAndEmailAreNull() {
        User invalidUser = new User("1", null, null,
                "DOC-123456", fechaHoy, "3000000000", null, "1234",
                "Customer", BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.FIRST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.LAST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_EMPTY)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenEmailIsInvalidFormat() {
        User invalidUser = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3000000000", "juanesosoriooutl.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_INVALID)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryExceedsMaxAllowed() {
        User invalidUser = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3000000000", "a@b.com", "1234",
                "Customer", BigDecimal.valueOf(18_000_000.0));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_INVALID)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryIsNull() {
        User invalidUser = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3000000000", "1234",
                "Customer", "juanesosorio@outl.com", null);

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_EMPTY)
                )
                .verify();

        verify(userRepository, never()).existUserByDocumentId(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByDocumentId_ShouldDelegateAndReturn() {
        User user = new User("u1", "Ana", "Perez",
                "DOC-777", fechaHoy, "3009998888", "ana@examples.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        when(userRepository.findByDocumentId("DOC-777")).thenReturn(Mono.just(user));

        StepVerifier.create(userUseCase.findByDocumentId("DOC-777"))
                .expectNext(user)
                .verifyComplete();

        verify(userRepository, times(1)).findByDocumentId("DOC-777");
    }


    @Test
    void findIsExist_ShouldThrowUnauthorized_WhenNoMatch() {
        Login login = new Login("john@example.com", "badpass");
        when(userRepository.findIsExist("john@example.com", "badpass")).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.findIsExist(login))
                .expectError(UnauthorizedException.class)
                .verify();

        verify(userRepository).findIsExist("john@example.com", "badpass");
    }

    @Test
    void findIsExist_ShouldReturnUser_WhenCredentialsMatch() {
        Login login = new Login("john@example.com", "secret");
        User repoUser = new User("u1", "John", "Doe",
                "DOC-1", fechaHoy, "300", "john@example.com", "secret", "Customer", BigDecimal.valueOf(1000));

        when(userRepository.findIsExist("john@example.com", "secret")).thenReturn(Mono.just(repoUser));

        StepVerifier.create(userUseCase.findIsExist(login))
                .expectNext(repoUser)
                .verifyComplete();

        verify(userRepository).findIsExist("john@example.com", "secret");
    }


    @Test
    void createUser_ShouldSucceed_WhenUserIsValid() {
        User user = new User("1", "juan", "carlos",
                "DOC-123456", fechaHoy, "3002223333", "john@examples.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        when(userRepository.existUserByDocumentId("DOC-123456")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("john@examples.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return Mono.just(u);
        });

        StepVerifier.create(userUseCase.createUser(user))
                .expectNextMatches(savedUser ->
                        savedUser.getUserId() != null &&
                                savedUser.getEmail().equals("john@examples.com") &&
                                savedUser.getDocumentId().equals("DOC-123456")
                )
                .verifyComplete();

        verify(userRepository).existUserByDocumentId("DOC-123456");
        verify(userRepository).existsByEmail("john@examples.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUsersByEmails_ShouldReturnUsers() {
        Set<String> emails = Set.of("a@ex.com", "b@ex.com");
        User u1 = new User("1", "A", "A", "D1", fechaHoy, "300", "a@ex.com", "p", "Customer", BigDecimal.TEN);
        User u2 = new User("2", "B", "B", "D2", fechaHoy, "301", "b@ex.com", "p", "Customer", BigDecimal.ONE);

        when(userRepository.getUsersByEmails(emails)).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(userUseCase.getUsersByEmails(emails))
                .expectNext(u1)
                .expectNext(u2)
                .verifyComplete();

        verify(userRepository).getUsersByEmails(emails);
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        User user = new User("1", "juan", "carlos",
                "DOC-777", fechaHoy, "3009998888", "john@examples.com", "1234",
                "Customer", BigDecimal.valueOf(1000));

        when(userRepository.findAll()).thenReturn(Flux.just(user));

        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(user)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}