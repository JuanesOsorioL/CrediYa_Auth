package co.com.crediya.r2dbc.user;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);

    Mono<User> findByDocumentId(String documentId);

    Flux<User> findByEmailIn(Collection<String> emails);

    Mono<Boolean> existsByDocumentId(String documentId);

    Mono<User> findByEmailAndPassword(String email, String password);

}

