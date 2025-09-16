package co.com.crediya.usecase.rol;

import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.rol.gateways.RolRepository;
import co.com.crediya.usecase.logger.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RolUseCaseTest {

    private RolRepository rolRepository;
    private Logger logger;
    private RolUseCase useCase;

    @BeforeEach
    void setUp() {
        rolRepository = Mockito.mock(RolRepository.class);
        logger = Mockito.mock(Logger.class);
        useCase = new RolUseCase(rolRepository, logger);
    }

    @Test
    void findById_returnsRolAndLogs() {
        Rol rol = Mockito.mock(Rol.class);
        when(rolRepository.findByRolId("ROL_1")).thenReturn(Mono.just(rol));

        StepVerifier.create(useCase.findById("ROL_1"))
                .expectNext(rol)
                .verifyComplete();

        verify(rolRepository).findByRolId("ROL_1");
        verify(logger, atLeastOnce()).info(contains("RolUseCase -> findById"));
    }

    @Test
    void findAll_returnsFluxAndLogsEach() {
        Rol r1 = Mockito.mock(Rol.class);
        Rol r2 = Mockito.mock(Rol.class);

        when(rolRepository.findAll()).thenReturn(Flux.just(r1, r2));

        StepVerifier.create(useCase.findAll())
                .expectNext(r1)
                .expectNext(r2)
                .verifyComplete();

        verify(rolRepository).findAll();
        verify(logger, atLeastOnce()).info(contains("Se buscan todos los roles"));
    }

    @Test
    void findById_propagatesError() {
        when(rolRepository.findByRolId(anyString())).thenReturn(Mono.error(new RuntimeException("db down")));

        StepVerifier.create(useCase.findById("X"))
                .expectErrorMessage("db down")
                .verify();

        verify(rolRepository).findByRolId("X");
    }
}