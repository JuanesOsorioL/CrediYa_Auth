package co.com.crediya.usecase.user;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.exception.UserErrorCode;
import co.com.crediya.usecase.user.exception.UserValidationException;
import co.com.crediya.usecase.user.logger.Logger;
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
    private final Logger logger;

    private static final BigDecimal SALARY_MIN = BigDecimal.ZERO;
    private static final BigDecimal SALARY_MAX = new BigDecimal("15000000");
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public Mono<User> createUser(User user) {
        logger.info("Iniciando validaciones de Dominio");
        List<UserErrorCode> errors = new ArrayList<>();
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            errors.add(UserErrorCode.FIRST_NAME_EMPTY);
            logger.info("Nombre vació");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            errors.add(UserErrorCode.LAST_NAME_EMPTY);
            logger.info("Apellido vació");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.add(UserErrorCode.EMAIL_EMPTY);
            logger.info("Email vació");
        } else if (!EMAIL_REGEX.matcher(user.getEmail()).matches()) {
            errors.add(UserErrorCode.EMAIL_INVALID);
            logger.info("Email Invalido");
        }
        if (user.getBaseSalary() == null) {
            errors.add(UserErrorCode.BASE_SALARY_EMPTY);
            logger.info("Salario vació");
        } else if (user.getBaseSalary().compareTo(SALARY_MIN) < 0 || user.getBaseSalary().compareTo(SALARY_MAX) > 0) {
            errors.add(UserErrorCode.BASE_SALARY_INVALID);
            logger.info("Salario Invalido");
        }
        if (!errors.isEmpty()) {
            logger.info("Se Genero Lista de Errores de Validacion");
            return Mono.error(new UserValidationException(List.of(), errors));
        }

        return userRepository.existUserByDocumentId(user.getDocumentId())
                .doOnSubscribe(u -> logger.info("Se Verifica si ya existe el documento de identidad"))
                        .flatMap(exist -> {
                                    if (exist) {
                                        logger.info("Documento de identidad ya existe");
                                        return Mono.error(new UserValidationException(List.of(), List.of(UserErrorCode.DOCUMENT_ALREADY_REGISTERED)));
                                    }

        logger.info("Se Verifica si correo ya existe");
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        logger.info("Correo si existe");
                        return Mono.error(new UserValidationException(List.of(), List.of(UserErrorCode.EMAIL_ALREADY_REGISTERED)));
                    }
                    logger.info("Correo No existe, se agrega un UUID para guardarlo");
                    User withId = user.toBuilder()
                            .userId(UUID.randomUUID().toString())
                            .build();
                    return userRepository.save(withId)
                            .doOnNext(u -> logger.info("Usuario guardado con id "));
                });
                        });
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll()
                .doOnNext(u -> logger.info("Se buscan usuarios"));
    }

    @Override
    public Mono<User> findByDocumentId(String documentId) {
        return userRepository.findByDocumentId(documentId)
                .doOnNext(u -> logger.info("Se buscan usuario por medio del documento"));
    }


}
