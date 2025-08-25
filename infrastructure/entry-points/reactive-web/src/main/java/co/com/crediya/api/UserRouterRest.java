package co.com.crediya.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return route(POST("/api/v1/user"), handler::createUser);
           //    .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase)
            //    .and(route(GET("/api/otherusercase/path"), handler::listenGETOtherUseCase));
    }
}
