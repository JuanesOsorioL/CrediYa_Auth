package co.com.crediya.config;


import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.UserUseCase;
import co.com.crediya.usecase.user.logger.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCasesConfigTest {

    @Test
    void guardarUsuarioUseCaseBeanIsCreatedTest() {
        UserRepository usuarioRepository = Mockito.mock(UserRepository.class);
        Logger logger = Mockito.mock(Logger.class);


        UseCasesConfig config = new UseCasesConfig();
        UserUseCase userCase = config.userUseCase(usuarioRepository,logger);
        assertNotNull(userCase);
    }
}
