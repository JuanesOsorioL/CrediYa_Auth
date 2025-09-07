package co.com.crediya.model.rol.gateways;

import co.com.crediya.model.rol.Rol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolRepository {
    Flux<Rol> findAll();

    Mono<Rol> findByRolId(String rolId);
}
