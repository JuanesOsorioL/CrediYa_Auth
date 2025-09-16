package co.com.crediya.api.config;


import co.com.crediya.api.segurity.filter.AuthFilter;
import co.com.crediya.api.segurity.jwt.AuthenticationService;
import co.com.crediya.usecase.logger.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebConfigTest {

    @Test
    void authFilterBean_isCreated_andPassesWhitelistedPath() {
        Logger logger = mock(Logger.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);

        WebConfig config = new WebConfig(logger);
        WebFilter filter = config.authFilter(authenticationService);

        assertThat(filter).isInstanceOf(AuthFilter.class);

        var req = MockServerHttpRequest.method(HttpMethod.POST, "/api/v1/login").build();
        var exchange = MockServerWebExchange.from(req);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        verify(chain, times(1)).filter(any());
        verifyNoInteractions(authenticationService);
    }
}