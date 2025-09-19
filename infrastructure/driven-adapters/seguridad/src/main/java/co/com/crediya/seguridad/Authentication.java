package co.com.crediya.seguridad;


import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.segurity.dto.TokenClaims;
import co.com.crediya.seguridad.mapper.AuthenticationMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class Authentication implements SegurityGateway {

    private final Logger logger;
    private final AuthenticationMapper authenticationMapper;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;


    private SecretKey getSingInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Mono<Token> generateToken(TokenClaims tokenClaims) {
        logger.info("AuthenticationService -> generateToken : se genera token");
        Map<String, Object> claims = Map.of(
                "FistName", tokenClaims.firstName(),
                "LastName", tokenClaims.lastName(),
                "Document", tokenClaims.documentId(),
                "Rol", tokenClaims.rolName()
        );
        return Mono.just(new Token(Jwts.builder()
                .id(tokenClaims.userId())
                .claims(claims)
                .subject(tokenClaims.email())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSingInKey())
                .compact()));

    }

    public Claismo validateTokenClaims(Token token) {
        logger.info("AuthenticationService -> validateTokenAndGetClaims : se valida token");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token.token())
                    .getPayload();
            logger.info("AuthenticationService -> validateTokenAndGetClaims : contenido del token Ahora es un Claismo : " + claims.toString());
            return authenticationMapper.toClaismo(claims);
        } catch (JwtException e) {
            return null;
        }
    }

}