package co.com.crediya.usecase.user;

import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.ConflictException;
import co.com.crediya.model.exception.specific_exceptions.NotFoundException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.segurity.dto.TokenClaims;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.rol.gateways.RolService;
import co.com.crediya.usecase.user.gateways.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UserUseCase implements UserService {

    public static final String NO_ENCONTRADO_USUARIO_O_CONTRASENA_NO_VALIDOS = "UserUseCase -> findIsExist : Cliente no encontrado, usuario o contraseña no validos";
    public static final String ENCONTRO_CLIENTE = "UserUseCase -> findById : Se encontro cliente";
    public static final String SE_CREA_EL_TOKEN_CLAIMS = "UserUseCase -> findIsExist : se crea el TokenClaims ";
    private final UserRepository userRepository;
    private final RolService rolService;
    private final Logger logger;

    private static final BigDecimal SALARY_MIN = BigDecimal.ZERO;
    private static final BigDecimal SALARY_MAX = new BigDecimal("15000000");
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    //->Verifica si usuario existe enviando email y pass, retorna el usuario
    public Mono<TokenClaims> findIsExist(Login login) {
        logger.info("UserUseCase -> findIsExist : se verifica si existe usuario con el email : " + login.getEmail() + " y password.");
        return userRepository.findIsExist(login.getEmail(), login.getPassword())
                .switchIfEmpty(Mono.defer(() -> {
                    logger.info(NO_ENCONTRADO_USUARIO_O_CONTRASENA_NO_VALIDOS);
                    return Mono.error(new NotFoundException(UserErrorCode.LOGUIN_FAIL_USER_NOT_FOUND));
                })).doOnNext(rol -> logger.info(ENCONTRO_CLIENTE))
                .flatMap(user -> rolService.findById(user.getRolId())
                        .map(role -> new TokenClaims(
                                user.getUserId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getDocumentId(),
                                user.getEmail(),
                                role.getName()
                        ))).doOnNext(u -> logger.info(SE_CREA_EL_TOKEN_CLAIMS + u.toString()));
    }

    @Override
    //->se crea usuario, retorna el usuario
    public Mono<User> createUser(User user) {
        logger.info("UserUseCase -> createUser : Iniciando validaciones de Dominio");
        List<UserErrorCode> errors = new ArrayList<>();
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            errors.add(UserErrorCode.FIRST_NAME_EMPTY);
            logger.info("UserUseCase -> createUser : Nombre vació");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            errors.add(UserErrorCode.LAST_NAME_EMPTY);
            logger.info("UserUseCase -> createUser : Apellido vació");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.add(UserErrorCode.EMAIL_EMPTY);
            logger.info("UserUseCase -> createUser : Email vació");
        } else if (!EMAIL_REGEX.matcher(user.getEmail()).matches()) {
            errors.add(UserErrorCode.EMAIL_INVALID);
            logger.info("UserUseCase -> createUser : Email Invalido");
        }
        if (user.getBaseSalary() == null) {
            errors.add(UserErrorCode.BASE_SALARY_EMPTY);
            logger.info("UserUseCase -> createUser : Salario vació");
        } else if (user.getBaseSalary().compareTo(SALARY_MIN) < 0 || user.getBaseSalary().compareTo(SALARY_MAX) > 0) {
            errors.add(UserErrorCode.BASE_SALARY_INVALID);
            logger.info("UserUseCase -> createUser : Salario Invalido");
        }
        if (!errors.isEmpty()) {
            logger.info("UserUseCase -> createUser : Se Genero Lista de Errores de Validacion");
            return Mono.error(new UserValidationException(List.of(), errors));
        }

        return Mono.just(user)
                .doOnNext(u -> logger.info("UserUseCase -> createUser : Se Verifica Si existe el documento de identidad"))
                .filterWhen(u -> userRepository.existUserByDocumentId(u.getDocumentId())
                        .map(exists -> !exists))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.info("UserUseCase -> createUser : Documento de identidad sí existe");
                    return Mono.error(new ConflictException(UserErrorCode.DOCUMENT_ALREADY_REGISTERED));
                })).doOnNext(u -> logger.info("UserUseCase -> createUser : Documento de identidad No existe"))
                .doOnNext(u -> logger.info("UserUseCase -> createUser : Se Verifica Si correo existe"))
                .filterWhen(u -> userRepository.existsByEmail(u.getEmail())
                        .map(exists -> !exists))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.info("UserUseCase - > createUser : Correo si existe");
                    return Mono.error(new ConflictException(UserErrorCode.EMAIL_ALREADY_REGISTERED));
                })).doOnNext(u -> logger.info("UserUseCase -> createUser : Correo No existe"))
                .map(u -> u.toBuilder().userId(UUID.randomUUID().toString()).build())
                .flatMap(userRepository::save)
                .doOnSuccess(u -> logger.info("UserUseCase -> createUser : createUser : Usuario guardado con id " + u));
    }

    @Override
    //->se busca usuario por documento, retorna el usuario
    public Mono<User> findByDocumentId(String documentId) {
        return userRepository.findByDocumentId(documentId)
                .doOnNext(u -> logger.info("UserUseCase -> findByDocumentId : Se buscan usuario por medio del documento"));
    }

    @Override
    //-> No se pidio, se trtae todos los usuarios
    public Flux<User> getAllUsers() {
        return userRepository.findAll()
                .doOnNext(u -> logger.info("UserUseCase -> getAllUsers : Se buscan usuarios (No se pidio en los requerimientos)"));
    }

    @Override
    //-> consulta los usuarios, con una lista de correos, retorna un flux de ususarios
    public Flux<User> getUsersByEmails(Set<String> emails) {
        return userRepository.getUsersByEmails(emails)
                .doOnNext(u -> logger.info("UserUseCase -> getUsersByEmails : Se buscan usuarios con base a la lista de emails"));
    }
}