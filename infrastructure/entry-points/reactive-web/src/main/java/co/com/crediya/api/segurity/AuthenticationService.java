package co.com.crediya.api.segurity;

import co.com.crediya.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;


@Component
public class AuthenticationService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;


    public String generateToken(User user) {
        Map<String, Object> claims = Map.of(
                "FistName", user.getFirstName(),
                "LastName", user.getLastName(),
                "Role", user.getRolId()
        );
        return Jwts.builder()
                .id(user.getUserId())
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSingInKey())
                .compact();

    }


    private SecretKey getSingInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims validateTokenAndGetClaims(String token) {
        return new Claims() {
            @Override
            public String getIssuer() {
                return "";
            }

            @Override
            public String getSubject() {
                return "";
            }

            @Override
            public Set<String> getAudience() {
                return Set.of();
            }

            @Override
            public Date getExpiration() {
                return null;
            }

            @Override
            public Date getNotBefore() {
                return null;
            }

            @Override
            public Date getIssuedAt() {
                return null;
            }

            @Override
            public String getId() {
                return "";
            }

            @Override
            public <T> T get(String s, Class<T> aClass) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public Object get(Object key) {
                return null;
            }

            @Override
            public Object put(String key, Object value) {
                return null;
            }

            @Override
            public Object remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ?> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<String> keySet() {
                return Set.of();
            }

            @Override
            public Collection<Object> values() {
                return List.of();
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return Set.of();
            }
        };
    }
/*
    public Claims parseJwt(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())  // Usamos la clave secreta para verificar la firma
                    .parseClaimsJws(token)  // Parseamos y validamos el JWT
                    .getBody();  // Devuelve el cuerpo (claims) del token
        } catch (JwtException e) {

            return null;  // Si ocurre una excepción, el token no es válido
        }
    }*/
}
