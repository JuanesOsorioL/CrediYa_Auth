package co.com.crediya.r2dbc.rol;

import co.com.crediya.model.rol.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RolReactiveRepositoryAdapterTest {

    private RolReactiveRepository repository;
    private ObjectMapper mapper;
    private RolReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RolReactiveRepository.class);
        mapper = Mockito.mock(ObjectMapper.class);
        adapter = new RolReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void findByRolId_delegateToRepository_success() {
        Rol rol = Mockito.mock(Rol.class);
        when(repository.findByRolId("ROL_1")).thenReturn(Mono.just(rol));

        StepVerifier.create(adapter.findByRolId("ROL_1"))
                .expectNext(rol)
                .verifyComplete();

        verify(repository).findByRolId("ROL_1");
        verifyNoInteractions(mapper);
    }

    @Test
    void findByRolId_propagatesError() {
        when(repository.findByRolId(anyString())).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(adapter.findByRolId("ANY"))
                .expectErrorMessage("boom")
                .verify();

        verify(repository).findByRolId("ANY");
    }
}