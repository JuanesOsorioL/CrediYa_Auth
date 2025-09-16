package co.com.crediya.api.segurity.jwt;

import co.com.crediya.api.dto.login.TokenClaimsDto;
import co.com.crediya.api.dto.login.TokenDto;
import co.com.crediya.api.logger.GlobalLogger;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationService service;
    private GlobalLogger logger;

    private static String generateBase64HmacSecret(int bytes) {
        byte[] keyBytes = new byte[bytes];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @BeforeEach
    void setUp() {
        logger = Mockito.mock(GlobalLogger.class);
        service = new AuthenticationService(logger);

        String base64Secret = generateBase64HmacSecret(64);
        ReflectionTestUtils.setField(service, "secretKey", base64Secret);
        ReflectionTestUtils.setField(service, "expirationTime", 60_000L);
    }

    @Test
    void generateToken_and_validate_success() {
        TokenClaimsDto claimsDto = new TokenClaimsDto(
                "user-123",
                "Ada",
                "Lovelace",
                "CC-999",
                "ada@crediya.com",
                "Admin"
        );

        TokenDto token = service.generateToken(claimsDto);
        assertThat(token).isNotNull();
        assertThat(token.token()).isNotBlank();

        Claims claims = service.validateTokenAndGetClaims(token);
        assertThat(claims).isNotNull();

        assertThat(claims.getId()).isEqualTo("user-123");
        assertThat(claims.getSubject()).isEqualTo("ada@crediya.com");

        assertThat(claims.get("FistName", String.class)).isEqualTo("Ada");
        assertThat(claims.get("LastName", String.class)).isEqualTo("Lovelace");
        assertThat(claims.get("Document", String.class)).isEqualTo("CC-999");
        assertThat(claims.get("Rol", String.class)).isEqualTo("Admin");

        verify(logger, atLeastOnce()).info(startsWith("AuthenticationService"));
    }

    @Test
    void validateToken_returnsNull_whenTokenIsInvalid() {
        Claims result = service.validateTokenAndGetClaims(new TokenDto("definitely-not-a-jwt"));
        assertThat(result).isNull();
    }

    @Test
    void validateToken_returnsNull_whenTokenIsExpired() {
        ReflectionTestUtils.setField(service, "expirationTime", -1_000L);

        TokenClaimsDto claimsDto = new TokenClaimsDto(
                "user-1", "John", "Doe", "DOC-1", "john@doe.com", "Customer"
        );

        TokenDto expired = service.generateToken(claimsDto);
        Claims parsed = service.validateTokenAndGetClaims(expired);

        assertThat(parsed).isNull();
    }
}