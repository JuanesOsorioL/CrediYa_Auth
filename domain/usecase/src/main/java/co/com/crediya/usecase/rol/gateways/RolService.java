package co.com.crediya.usecase.rol.gateways;

import co.com.crediya.model.rol.Rol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolService {

    Mono<Rol> findById(String rolId);


    Flux<Rol> findAll();
}
