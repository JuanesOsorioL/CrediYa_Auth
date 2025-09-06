package co.com.crediya.api.segurity;

import co.com.crediya.api.dto.login.TokenClaimsDto;
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

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;


    private SecretKey getSingInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }


    public String generateToken(TokenClaimsDto tokenClaimsDto) {
        Map<String, Object> claims = Map.of(
                "FistName", tokenClaimsDto.firstName(),
                "LastName", tokenClaimsDto.lastName(),
                "Document", tokenClaimsDto.documentId(),
                "Rol", tokenClaimsDto.rolName()
        );
        return Jwts.builder()
                .id(tokenClaimsDto.userId())
                .claims(claims)
                .subject(tokenClaimsDto.email())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSingInKey())
                .compact();

    }


//    public boolean validateToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            if (claims != null) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (JwtException e) {
//            return false;
//        }
//    }

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parser() //Jwts.parserBuilder()
                    .verifyWith(getSingInKey())//   .setSigningKey(getSingInKey())
                    .build()//   .build()
                    .parseSignedClaims(token)//  .parseClaimsJws(token)
                    .getPayload(); // .getBody();
        } catch (JwtException e) {
            return null;
        }
    }


//
//    public Claims validateTokenDos(String token) {
//        try {
//             return Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//        } catch (JwtException e) {
//            return null;
//        }
//    }

//
//    public Claims validateTokenDosss(String token) {
//        try {
//
//            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
//
//            //OK, we can trust this JWT
//
//        } catch (JwtException e) {
//
//            //don't trust the JWT!
//        }
//    }


}