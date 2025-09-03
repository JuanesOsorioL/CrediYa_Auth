package co.com.crediya.usecase.user;

import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> createUser(User user);

    Flux<User> getAllUsers();

    Mono<User> findByDocumentId(String documentId);

    Mono<User> findIsExist(Login login);
}
