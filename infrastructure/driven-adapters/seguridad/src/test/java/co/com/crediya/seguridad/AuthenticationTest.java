package co.com.crediya.seguridad;

import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.segurity.dto.TokenClaims;
import co.com.crediya.seguridad.mapper.AuthenticationMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class AuthenticationTest {
    private Authentication authentication;
    private Logger logger;
    private AuthenticationMapper mapper;

    private static String generateBase64HmacSecret(int bytes) {
        byte[] keyBytes = new byte[bytes];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @BeforeEach
    void setUp() {
        logger = Mockito.mock(Logger.class);
        mapper = Mockito.mock(AuthenticationMapper.class);
        authentication = new Authentication(logger, mapper);

        String base64Secret = generateBase64HmacSecret(64);
        ReflectionTestUtils.setField(authentication, "secretKey", base64Secret);
        ReflectionTestUtils.setField(authentication, "expirationTime", 60_000L);
    }

    @Test
    void generateToken_and_validate_success() {
        TokenClaims claimsDto = new TokenClaims(
                "user-123",
                "Ada",
                "Lovelace",
                "CC-999",
                "ada@crediya.com",
                "Admin"
        );

        Token token = authentication.generateToken(claimsDto);
        assertThat(token).isNotNull();
        assertThat(token.token()).isNotBlank();

        when(mapper.toClaismo(any(Claims.class))).thenAnswer(inv -> {
            Claims c = inv.getArgument(0, Claims.class);
            return new Claismo(
                    c.get("FistName", String.class),
                    c.getSubject(),
                    c.get("Rol", String.class),
                    c.getExpiration() == null ? null : String.valueOf(c.getExpiration().toInstant().getEpochSecond()),
                    c.get("LastName", String.class),
                    c.get("Document", String.class),
                    c.getIssuedAt() == null ? null : String.valueOf(c.getIssuedAt().toInstant().getEpochSecond()),
                    c.getId()
            );
        });

        Claismo out = authentication.validateTokenClaims(token);

        assertThat(out).isNotNull();
        assertThat(out.FistName()).isEqualTo("Ada");
        assertThat(out.sub()).isEqualTo("ada@crediya.com");
        assertThat(out.Rol()).isEqualTo("Admin");
        assertThat(out.LastName()).isEqualTo("Lovelace");
        assertThat(out.Document()).isEqualTo("CC-999");
        assertThat(out.jti()).isEqualTo("user-123");
        assertThat(out.iat()).isNotBlank();
        assertThat(out.exp()).isNotBlank();

        verify(logger, atLeastOnce()).info(startsWith("AuthenticationService"));
        verify(mapper).toClaismo(any(Claims.class));
    }

    @Test
    void validateToken_returnsNull_whenTokenIsInvalid() {
        Claismo result = authentication.validateTokenClaims(new Token("definitely-not-a-jwt"));
        assertThat(result).isNull();

        verifyNoInteractions(mapper);
    }

    @Test
    void validateToken_returnsNull_whenTokenIsExpired() {
        ReflectionTestUtils.setField(authentication, "expirationTime", -1_000L);

        TokenClaims claimsDto = new TokenClaims(
                "user-1", "John", "Doe", "DOC-1", "john@doe.com", "Customer"
        );

        Token expired = authentication.generateToken(claimsDto);
        Claismo parsed = authentication.validateTokenClaims(expired);

        assertThat(parsed).isNull();
    }
}