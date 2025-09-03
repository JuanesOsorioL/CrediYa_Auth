package co.com.crediya.api.filter;

import co.com.crediya.api.segurity.AuthenticationService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class AuthFilter implements WebFilter{

    private final AuthenticationService authenticationService;

    public AuthFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);


            Claims claims = authenticationService.validateTokenAndGetClaims(token);

            if (claims != null) {
                // Extraer el rol del token
                String role = claims.get("Role", String.class);

                // Verificar el rol según el endpoint (esto es solo un ejemplo)
                if (isValidRoleForEndpoint(role, (ServerRequest) exchange.getRequest())) {/// mirar
                    return chain.filter(exchange);  // Continuar con la cadena de filtros si el rol es válido
                } else {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso"));
                }
            }
        }

        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no proporcionado"));
    }

    // Método para validar el rol según el endpoint
    private boolean isValidRoleForEndpoint(String role, ServerRequest request) {
        // Dependiendo del endpoint, validamos el rol requerido
        if (request.uri().getPath().contains("/api/v1/users")) {
            return role.equals("admin") || role.equals("advisor");  // Solo admin o asesor pueden crear usuarios
        }

        if (request.uri().getPath().contains("/api/v1/loan")) {
            return role.equals("client");  // Solo los clientes pueden crear solicitudes de préstamo
        }

        return false;
    }
}