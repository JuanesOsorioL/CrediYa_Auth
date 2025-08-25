package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UserUseCase implements UserService {

    private final UserRepository userRepository;

    private static final BigDecimal SALARY_MIN = BigDecimal.ZERO;
    private static final BigDecimal SALARY_MAX = new BigDecimal("15000000");
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");


    @Override
    public Mono<User> createUser(User user) {
        return validateUser(user)
                .then(userRepository.existsByEmail(user.getEmail()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Email is already registered."));
                    }
                    return userRepository.save(user);
                });
    }

    private Mono<Void> validateUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("First name is required."));
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Last name is required."));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email is required."));
        }
        if (!EMAIL_REGEX.matcher(user.getEmail()).matches()) {
            return Mono.error(new IllegalArgumentException("Email format is invalid."));
        }
        if (user.getBaseSalary() == null) {
            return Mono.error(new IllegalArgumentException("Base salary is required."));
        }
        if (user.getBaseSalary().compareTo(SALARY_MIN) < 0 || user.getBaseSalary().compareTo(SALARY_MAX) > 0) {
            return Mono.error(new IllegalArgumentException("Base salary must be between 0 and 15,000,000."));
        }
        return Mono.empty();
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
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found."))).flatMap(existingUser -> {
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
