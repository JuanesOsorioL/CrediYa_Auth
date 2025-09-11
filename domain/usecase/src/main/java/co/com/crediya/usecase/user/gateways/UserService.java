package co.com.crediya.usecase.user.gateways;

import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface UserService {

    //->Verifica si usuario existe enviando email y pass, retorna el usuario
    Mono<User> findIsExist(Login login);

    //->se crea usuario, retorna el usuario
    Mono<User> createUser(User user);

    //->se busca usuario por documento, retorna el usuario
    Mono<User> findByDocumentId(String documentId);

    //-> No se pidio, se trtae todos los usuarios
    Flux<User> getAllUsers();

    //-> consulta los usuarios, con una lista de correos, retorna un flux de ususarios
    Flux<User> getUsersByEmails(Set<String> emails);
}
