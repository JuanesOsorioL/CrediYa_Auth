package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface UserRepository {
    //->Verifica si usuario existe enviando email y pass, retorna el usuario
    Mono<User> findIsExist(String email, String password);

    //->Verifica si usuario existe enviando documento, retorna boolean
    Mono<Boolean> existUserByDocumentId(String documentId);

    //->Verifica si usuario existe enviando email, retorna boolean
    Mono<Boolean> existsByEmail(String email);

    //->se guarda usuario, retorna el usuario guardado
    Mono<User> save(User user);

    //->se busca usuario por documento, retorna el usuario
    Mono<User> findByDocumentId(String documentId);

    //-> No se pidio, se trtae todos los usuarios
    Flux<User> findAll();

    //-> consulta los usuarios, con una lista de correos, retorna un flux de ususarios
    Flux<User> getUsersByEmails(Set<String> emails);
}
