
package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;

import co.com.crediya.usecase.user.exception.UserErrorCode;
import co.com.crediya.usecase.user.exception.UserValidationException;
import co.com.crediya.usecase.user.logger.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        userUseCase = new UserUseCase(userRepository,logger);
    }

    @Test
    void createUser_ShouldFail_WhenFirstNameLastNameAndEmailAreEmpty() {
        User invalidUser = new User("1", "", "", fechaHoy, "123456", "", BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.FIRST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.LAST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_EMPTY)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryIsInvalid() {
        User invalidUser = new User("1", "", "", fechaHoy, "123456", "", BigDecimal.valueOf(-1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_INVALID)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenFirstNameLastNameAndEmailAreNull() {
        User invalidUser = new User("1", null, null, fechaHoy, "123456", null, BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.FIRST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.LAST_NAME_EMPTY) &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_EMPTY)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenEmailIsInvalidFormat() {
        User invalidUser = new User("1", "null", "null", fechaHoy, "123456", "juanesosoriooutl.com", BigDecimal.valueOf(1000));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_INVALID)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryExceedsMaxAllowed() {
        User invalidUser = new User("1", "", "", fechaHoy, "123456", "", BigDecimal.valueOf(18000000.0));

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_INVALID)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenBaseSalaryIsNull() {
        User invalidUser = new User("1", "juan", "carlos", fechaHoy, "123456", "juanesosorio@outl.com", null);

        StepVerifier.create(userUseCase.createUser(invalidUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.BASE_SALARY_EMPTY)
                )
                .verify();
    }

    @Test
    void createUser_ShouldFail_WhenEmailAlreadyExists() {
        User user = new User("1", "juan", "carlos", fechaHoy, "123456", "john@example.com", BigDecimal.valueOf(1000));

        when(userRepository.existsByEmail("john@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.createUser(user))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserValidationException dve &&
                                dve.getDomainErrors().contains(UserErrorCode.EMAIL_ALREADY_REGISTERED)
                )
                .verify();

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldSucceed_WhenUserIsValid() {
        User user = new User("1", "juan", "carlos", fechaHoy, "123456", "john@examples.com", BigDecimal.valueOf(1000));

        when(userRepository.existsByEmail("john@examples.com")).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return Mono.just(u);
        });

        StepVerifier.create(userUseCase.createUser(user))
                .expectNextMatches(savedUser -> savedUser.getUserId() != null && savedUser.getEmail().equals("john@examples.com"))
                .verifyComplete();

        verify(userRepository).existsByEmail("john@examples.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        User user = new User("1", "juan", "carlos", fechaHoy, "123456", "john@examples.com", BigDecimal.valueOf(1000));

        when(userRepository.findAll()).thenReturn(Flux.just(user));

        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(user)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}
