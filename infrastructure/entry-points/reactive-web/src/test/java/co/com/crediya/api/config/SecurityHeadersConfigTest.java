package co.com.crediya.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityHeadersConfigTest {

    @Test
    void filter_setsSecurityHeaders() {
        SecurityHeadersConfig cfg = new SecurityHeadersConfig();

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/any").build());
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(cfg.filter(exchange, chain)).verifyComplete();

        var headers = exchange.getResponse().getHeaders();
        assertThat(headers.getFirst("Content-Security-Policy")).contains("default-src 'self'");
        assertThat(headers.getFirst("Strict-Transport-Security")).isEqualTo("max-age=31536000;");
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(headers.getFirst("Server")).isEqualTo("");
        assertThat(headers.getFirst("Cache-Control")).isEqualTo("no-store");
        assertThat(headers.getFirst("Pragma")).isEqualTo("no-cache");
        assertThat(headers.getFirst("Referrer-Policy")).isEqualTo("strict-origin-when-cross-origin");

        verify(chain, times(1)).filter(exchange);
    }
}