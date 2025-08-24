package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements UserService{
    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(User user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Email already exists"));
                    }
                    return userRepository.save(user);
                });
    }
    @Override
    public Mono<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public Mono<User> updateUser(String id, User userUpdate) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setFirstName(userUpdate.getFirstName());
                    existingUser.setLastName(userUpdate.getLastName());
                    existingUser.setBirthDate(userUpdate.getBirthDate());
                    existingUser.setAddress(userUpdate.getAddress());
                    existingUser.setPhone(userUpdate.getPhone());
                    existingUser.setEmail(userUpdate.getEmail());
                    existingUser.setBaseSalary(userUpdate.getBaseSalary());
                    return userRepository.save(existingUser);
                });
    }
    @Override
    public Mono<Void> deleteUser(String id) {
        return userRepository.deleteById(id);
    }
    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
