package co.com.crediya.api.segurity.jwt;

import co.com.crediya.api.dto.login.TokenClaimsDto;
import co.com.crediya.api.dto.login.TokenDto;
import co.com.crediya.api.logger.GlobalLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;


@Component
public class AuthenticationService {

    private final GlobalLogger logger;

    public AuthenticationService(GlobalLogger logger) {
        this.logger = logger;
    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;


    private SecretKey getSingInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public TokenDto generateToken(TokenClaimsDto tokenClaimsDto) {
        logger.info("AuthenticationService -> generateToken : se genera token");
        Map<String, Object> claims = Map.of(
                "FistName", tokenClaimsDto.firstName(),
                "LastName", tokenClaimsDto.lastName(),
                "Document", tokenClaimsDto.documentId(),
                "Rol", tokenClaimsDto.rolName()
        );
        return new TokenDto(Jwts.builder()
                .id(tokenClaimsDto.userId())
                .claims(claims)
                .subject(tokenClaimsDto.email())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSingInKey())
                .compact());

    }

    public Claims validateTokenAndGetClaims(TokenDto token) {
        logger.info("AuthenticationService -> validateTokenAndGetClaims : se valida token");
        try {
            return Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token.token())
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

}