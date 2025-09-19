package co.com.crediya.usecase.rol;

import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.ConflictException;
import co.com.crediya.model.exception.specific_exceptions.NotFoundException;
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
        logger.info("RolUseCase -> findById : Se busca rol por medio del id");
        return rolRepository.findByRolId(rolId)
                .switchIfEmpty(Mono.defer(() -> {
                    logger.info("RolUseCase -> findById : No se encontró el rol");
                    return Mono.error(new NotFoundException(UserErrorCode.ROL_NOT_FOUND));
                })).doOnNext(rol -> logger.info("RolUseCase -> findById : Rol encontrado"));
    }


    @Override
    public Flux<Rol> findAll() {
        return rolRepository.findAll()
                .doOnNext(rol -> logger.info("Se buscan todos los roles"));
    }


}
