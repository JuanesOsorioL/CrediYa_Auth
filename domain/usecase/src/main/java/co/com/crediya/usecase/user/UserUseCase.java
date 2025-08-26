package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
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
                    User userWithId = user.toBuilder()
                            .userId(UUID.randomUUID().toString())
                            .build();
                    return userRepository.save(userWithId);
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
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

}
