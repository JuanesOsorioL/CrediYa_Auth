package co.com.crediya.config;

import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.user.UserUseCase;
import co.com.crediya.usecase.logger.Logger;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(basePackages = "co.com.crediya.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)

public class UseCasesConfig {
        @Bean
        @Primary
        public UserUseCase userUseCase(UserRepository userRepository,Logger logger) {
                return new UserUseCase(userRepository, logger);
        }
}
