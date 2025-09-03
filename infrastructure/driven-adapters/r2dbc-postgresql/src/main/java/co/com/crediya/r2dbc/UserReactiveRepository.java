package co.com.crediya.r2dbc;

import co.com.crediya.model.user.User;
import co.com.crediya.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);
    Mono<User> findByDocumentId(String documentId);
    Mono<Boolean> existsByDocumentId(String documentId);
}
