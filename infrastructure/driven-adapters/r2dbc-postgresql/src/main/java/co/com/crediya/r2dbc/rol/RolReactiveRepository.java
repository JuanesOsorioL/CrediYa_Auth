package co.com.crediya.r2dbc.rol;

import co.com.crediya.model.rol.Rol;
import co.com.crediya.r2dbc.entities.RolEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RolReactiveRepository extends ReactiveCrudRepository<RolEntity, String>, ReactiveQueryByExampleExecutor<RolEntity> {

    Mono<Rol> findByRolId(String rolId);
}
