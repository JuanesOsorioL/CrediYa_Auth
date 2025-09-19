package co.com.crediya.model.segurity;

import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.segurity.dto.TokenClaims;
import reactor.core.publisher.Mono;

public interface SegurityGateway {

    Mono<Token> generateToken(TokenClaims tokenClaims);

    Claismo validateTokenClaims(Token token);
}
