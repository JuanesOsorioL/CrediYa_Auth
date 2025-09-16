package co.com.crediya.usecase.rol;

import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.rol.gateways.RolRepository;
import co.com.crediya.usecase.rol.gateways.RolService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RolUseCase implements RolService {

    private final RolRepository rolRepository;
    private final Logger logger;

    @Override
    public Mono<Rol> findById(String rolId) {
        return rolRepository.findByRolId(rolId)
                .doOnNext(rol -> logger.info("RolUseCase -> findById : Se busca rol por medio del id"));
    }


    @Override
    public Flux<Rol> findAll() {
        return rolRepository.findAll()
                .doOnNext(rol -> logger.info("Se buscan todos los roles"));
    }


}
