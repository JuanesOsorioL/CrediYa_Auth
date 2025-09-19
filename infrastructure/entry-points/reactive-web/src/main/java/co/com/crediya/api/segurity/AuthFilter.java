package co.com.crediya.api.segurity;

import co.com.crediya.api.dto.segurity.TokenDto;
import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.model.exception.UserErrorCode;
import co.com.crediya.model.exception.specific_exceptions.ForbiddenException;
import co.com.crediya.model.exception.specific_exceptions.UnauthorizedException;
import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
import co.com.crediya.model.segurity.dto.Claismo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthFilter implements WebFilter {

    private final SegurityGateway segurityGateway;
    private final GenericDtoMapper genericDtoMapper;
    private final Logger logger;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.info("AuthFilter -> filter : inicia el flujo de validaciones del token");
        var request = exchange.getRequest();
        var path = request.getURI().getPath();
        var method = request.getMethod();

        if (isWhitelisted(path, method)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            header = header.substring(7);
            TokenDto token = new TokenDto(header);

            Claismo claismo = segurityGateway.validateTokenClaims(genericDtoMapper.toToken(token));

            if (claismo != null) {
                String role = claismo.Rol();
                logger.info("AuthFilter -> filter : rol ingresado : " + role);

                if (isValidRoleForEndpoint(role, request)) {
                    logger.info("AuthFilter -> filter : si cumple y puede ejecutar la solicitud");
                    exchange.getAttributes().put("claims", claismo);
                    return chain.filter(exchange);
                } else {
                    logger.info("AuthFilter -> filter : No tienes permisos para acceder a este recurso");
                    return Mono.error(new ForbiddenException(UserErrorCode.YOU_DONT_HAVE_PERMISSION));
                }
            } else {
                logger.info("AuthFilter -> filter : Token inválido, llega null");
                return Mono.error(new UnauthorizedException(UserErrorCode.TOKEN_INVALID));
            }
        } else {
            logger.info("AuthFilter -> filter : Token no proporcionado");
            return Mono.error(new UnauthorizedException(UserErrorCode.TOKEN_EMPTY));
        }
    }


    private boolean isValidRoleForEndpoint(String role, ServerHttpRequest request) {
        String path = request.getURI().getPath();

        if (path.startsWith("/api/v1/usuarios")) {
            return role.equals("Admin") || role.equals("Adviser");
        }

        if (path.startsWith("/api/v1/solicitud")) {
            return role.equals("Customer");
        }

        if (path.startsWith("/api/v1/validateToken") || path.startsWith("/api/v1/map") || path.startsWith("/api/v1/document")) {
            logger.info("AuthFilter -> isValidRoleForEndpoint : Llamado del micro");
            return role.equals("Admin") || role.equals("Adviser") || role.equals("Customer");
        }

        return false;
    }


    private boolean isWhitelisted(String path, HttpMethod method) {
        return path.startsWith("/api/v1/login")
                || path.startsWith("/api/v1/usuarios/allUsers")//no aplica a los criterios
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/CrediYa_Auth/build/reports/jacocoMergedReport")
                || path.startsWith("/webjars/swagger-ui")
                || path.startsWith("/actuator")
                || (method != null && method.name().equalsIgnoreCase("OPTIONS"));
    }
}