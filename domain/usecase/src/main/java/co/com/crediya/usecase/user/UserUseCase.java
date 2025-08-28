package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserErrorCode;
import co.com.crediya.usecase.user.exception.UserValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        List<UserErrorCode> errors = new ArrayList<>();
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            errors.add(UserErrorCode.FIRST_NAME_EMPTY);
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            errors.add(UserErrorCode.LAST_NAME_EMPTY);
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.add(UserErrorCode.EMAIL_EMPTY);
        }
        if (!EMAIL_REGEX.matcher(user.getEmail()).matches()) {
            errors.add(UserErrorCode.EMAIL_INVALID);
        }
        if (user.getBaseSalary() == null) {
            errors.add(UserErrorCode.BASE_SALARY_EMPTY);
        } else if (user.getBaseSalary().compareTo(SALARY_MIN) < 0 || user.getBaseSalary().compareTo(SALARY_MAX) > 0) {
            errors.add(UserErrorCode.BASE_SALARY_INVALID);
        }
        if (!errors.isEmpty()) {
            return Mono.error(new UserValidationException(List.of(), errors));
        }
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserValidationException(List.of(), List.of(UserErrorCode.EMAIL_ALREADY_REGISTERED)));
                    }
                    User withId = user.toBuilder()
                            .userId(UUID.randomUUID().toString())
                            .build();
                    return userRepository.save(withId);
                });
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

}
