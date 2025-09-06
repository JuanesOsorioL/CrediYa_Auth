package co.com.crediya.api.filter;

import co.com.crediya.api.segurity.AuthenticationService;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.usecase.exception.UserValidationException;
import co.com.crediya.usecase.logger.Logger;
import io.jsonwebtoken.Claims;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

public class AuthFilter implements WebFilter {

    private final AuthenticationService authenticationService;
    private final Logger logger;

    public AuthFilter(AuthenticationService authenticationService, Logger logger) {
        this.authenticationService = authenticationService;
        this.logger = logger;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getURI().getPath();
        var method = request.getMethod();

        if (path.startsWith("/api/v1/login")
                || path.startsWith("/api/v1/validateToken")
                || path.startsWith("/api/v1/usuarios/allUsers")
                || path.startsWith("/api/v1/usuarios/document")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/CrediYa_Auth/build/reports/jacocoMergedReport")
                || path.startsWith("/webjars/swagger-ui")
                || path.startsWith("/actuator")
                || (method != null && method.name().equalsIgnoreCase("OPTIONS"))) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            Claims claims = authenticationService.validateTokenAndGetClaims(token);

            if (claims != null) {
                String role = claims.get("Rol", String.class);
                logger.info(role);
                if (isValidRoleForEndpoint(role, request)) {
                    logger.info("si cumple y puede ejecutar la solicitud");
                    return chain.filter(exchange);
                } else {
                    logger.info("No tienes permisos para acceder a este recurso");
                    //  return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso"));
                    return Mono.error(new UserValidationException(List.of(UserErrorCode.YOU_DONT_HAVE_PERMISSION), List.of()));
                }
            } else {
                logger.info("Token inválido");
                // return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));
                return Mono.error(new UserValidationException(List.of(UserErrorCode.TOKEN_INVALID), List.of()));
            }
        } else {
            // Si no hay token
            logger.info("Token no proporcionado");
            // return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado"));
            return Mono.error(new UserValidationException(List.of(UserErrorCode.TOKEN_EMPTY), List.of()));
        }
    }


    private boolean isValidRoleForEndpoint(String role, ServerHttpRequest request) {

        String path = request.getURI().getPath();
        if (path.startsWith("/api/v1/usuarios")) {
            return role.equals("Admin") || role.equals("Advisor");
        }

        if (path.startsWith("/api/v1/solicitud")) {
            return role.equals("Customer");
        }
        return false;
    }
}
