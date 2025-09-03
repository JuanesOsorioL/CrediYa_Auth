package co.com.crediya.api;


import co.com.crediya.api.config.UserPath;
import co.com.crediya.api.exception.GlobalErrorHandler;
import co.com.crediya.api.openapi.UserOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class UserRouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler, GlobalErrorHandler errorHandler, UserPath userPath) {
        return route()
                .POST(userPath.getBase(), handler::createUser, UserOpenApi::createUser)
                .GET(userPath.getBase(), handler::findAll, UserOpenApi::findAll)
                .POST(userPath.getDocument(), handler::findByDocumentId, UserOpenApi::findByDocumentId)
                .build()
                .filter(errorHandler.filter());
    }
}