package co.com.crediya.api.config;


import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.segurity.AuthFilter;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
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
    public WebFilter authFilter(SegurityGateway segurityGateway, GenericDtoMapper genericDtoMapper) {
        return new AuthFilter(segurityGateway, genericDtoMapper, logger);
    }
}
