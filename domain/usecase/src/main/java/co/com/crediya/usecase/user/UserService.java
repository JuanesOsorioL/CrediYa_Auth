package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> createUser(User user);

    Mono<User> getUserById(String id);

    Flux<User> getAllUsers();

    Mono<User> updateUser(String id, User userUpdate);

    public Mono<Void> deleteUser(String id);

    public Mono<User> getUserByEmail(String email);

}
