package co.com.crediya.api.config;


import co.com.crediya.api.filter.AuthFilter;
import co.com.crediya.api.segurity.AuthenticationService;
import co.com.crediya.usecase.logger.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebConfig {

    private final Logger logger;

    public WebConfig(Logger logger) {
        this.logger = logger;
    }

    @Bean
    public WebFilter authFilter(AuthenticationService authenticationService) {
        return new AuthFilter(authenticationService,logger);
    }
}
