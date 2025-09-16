package co.com.crediya.api.config;


import co.com.crediya.api.mapper.GenericDtoMapper;
import co.com.crediya.api.segurity.AuthFilter;


import co.com.crediya.model.logger.Logger;
import co.com.crediya.model.segurity.SegurityGateway;
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
        SegurityGateway segurityGateway = mock(SegurityGateway.class);
        GenericDtoMapper genericDtoMapper = mock(GenericDtoMapper.class);

        WebConfig config = new WebConfig(logger);
        WebFilter filter = config.authFilter(segurityGateway, genericDtoMapper);

        assertThat(filter).isInstanceOf(AuthFilter.class);

        var req = MockServerHttpRequest.method(HttpMethod.POST, "/api/v1/login").build();
        var exchange = MockServerWebExchange.from(req);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        verify(chain, times(1)).filter(any());
        verifyNoInteractions(segurityGateway, genericDtoMapper);
    }
}