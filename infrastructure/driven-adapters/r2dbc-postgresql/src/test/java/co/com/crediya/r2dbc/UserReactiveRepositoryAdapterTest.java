package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.entities.UserEntity;
import co.com.crediya.r2dbc.user.UserReactiveRepository;
import co.com.crediya.r2dbc.user.UserReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {


    @InjectMocks
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private static final UserEntity entity = new UserEntity(
            "1",
            "John Doe",
            "Gomez",
            LocalDate.of(1990, 1, 1),
            "12345",
            "123456789",
            "Customer",
            "john@doe.com",
            "1234",
            BigDecimal.valueOf(18000000.0)
    );

    private static final User user = User.builder()
            .userId("1")
            .email("john@doe.com")
            .firstName("John Doe")
            .lastName("Gomez")
            .documentId("123456789")
            .birthDate(LocalDate.of(1990, 1, 1))
            .phone("123456789")
            .baseSalary(BigDecimal.valueOf(18000000.0))
            .build();

    @BeforeEach
    void setUp() {
        repositoryAdapter = new UserReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void mustFindValueById() {
        when(repository.findById("1")).thenReturn(Mono.just(entity));
        when(mapper.mapBuilder(entity, User.UserBuilder.class)).thenReturn(user.toBuilder());

        Mono<User> result = repositoryAdapter.findById("1");

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        when(repository.findAll()).thenReturn(Flux.just(entity));
        when(mapper.mapBuilder(entity, User.UserBuilder.class)).thenReturn(user.toBuilder());

        Flux<User> result = repositoryAdapter.findAll();

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustFindByExample() {

        when(mapper.map(user, UserEntity.class)).thenReturn(entity);

        when(repository.findAll(ArgumentMatchers.<Example<UserEntity>>any()))
                .thenReturn(Flux.just(entity));

        when(mapper.mapBuilder(entity, User.UserBuilder.class)).thenReturn(user.toBuilder());

        Flux<User> result = repositoryAdapter.findByExample(user);

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        when(mapper.map(user, UserEntity.class)).thenReturn(entity);

        when(repository.save(entity)).thenReturn(Mono.just(entity));

        when(mapper.mapBuilder(entity, User.UserBuilder.class)).thenReturn(user.toBuilder());

        Mono<User> result = repositoryAdapter.save(user);

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void mustExistByEmail() {
        when(repository.existsByEmail("john@doe.com")).thenReturn(Mono.just(true));

        Mono<Boolean> result = repositoryAdapter.existsByEmail("john@doe.com");

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void mustFindIsExist_success() {
        when(repository.findByEmailAndPassword("john@doe.com", "1234"))
                .thenReturn(Mono.just(user));

        StepVerifier.create(repositoryAdapter.findIsExist("john@doe.com", "1234"))
                .expectNext(user)
                .verifyComplete();

        verify(repository).findByEmailAndPassword("john@doe.com", "1234");

    }

    @Test
    void mustFindIsExist_emptyCompletes() {
        when(repository.findByEmailAndPassword("john@doe.com", "badpass"))
                .thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findIsExist("john@doe.com", "badpass"))
                .verifyComplete();

        verify(repository).findByEmailAndPassword("john@doe.com", "badpass");
    }

    @Test
    void mustExistUserByDocumentId_true() {
        when(repository.existsByDocumentId("123456789")).thenReturn(Mono.just(true));

        StepVerifier.create(repositoryAdapter.existUserByDocumentId("123456789"))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByDocumentId("123456789");
    }

    @Test
    void mustExistUserByDocumentId_false() {
        when(repository.existsByDocumentId("000")).thenReturn(Mono.just(false));

        StepVerifier.create(repositoryAdapter.existUserByDocumentId("000"))
                .expectNext(false)
                .verifyComplete();

        verify(repository).existsByDocumentId("000");
    }

    @Test
    void mustFindByDocumentId_success() {
        when(repository.findByDocumentId("123456789")).thenReturn(Mono.just(user));

        StepVerifier.create(repositoryAdapter.findByDocumentId("123456789"))
                .expectNext(user)
                .verifyComplete();

        verify(repository).findByDocumentId("123456789");
    }

    @Test
    void mustGetUsersByEmails_success() {
        Set<String> emails = Set.of("john@doe.com", "jane@doe.com");
        when(repository.findByEmailIn(emails)).thenReturn(Flux.just(user));

        StepVerifier.create(repositoryAdapter.getUsersByEmails(emails))
                .expectNext(user)
                .verifyComplete();

        verify(repository).findByEmailIn(emails);
    }

    @Test
    void mustGetUsersByEmails_propagatesError() {
        Set<String> emails = Set.of("john@doe.com");
        when(repository.findByEmailIn(emails)).thenReturn(Flux.error(new RuntimeException("db error")));

        StepVerifier.create(repositoryAdapter.getUsersByEmails(emails))
                .expectErrorMessage("db error")
                .verify();

        verify(repository).findByEmailIn(emails);
    }
}
