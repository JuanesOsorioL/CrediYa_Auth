package co.com.crediya.api;


import co.com.crediya.api.config.UserPath;
import co.com.crediya.api.openapi.UserOpenApi;
import co.com.crediya.model.logger.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final Logger logger;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler, UserPath userPath) {
        logger.info("RouterRest -> routerFunction : inicia el flujo");
        return route()
                .POST(userPath.getBase(), handler::createUser, UserOpenApi::createUser)
                .POST(userPath.getSomeUsers(), handler::getUsersMapEmails, UserOpenApi::someUsers)
                .GET(userPath.getAllUsers(), handler::findAll, UserOpenApi::findAll)
                .POST(userPath.getDocument(), handler::findByDocumentId, UserOpenApi::findByDocumentId)
                .POST(userPath.getLogin(), handler::login, UserOpenApi::login)
                .GET(userPath.getValidateToken(), handler::validateToken, UserOpenApi::validateToken)
                .build();
    }

}