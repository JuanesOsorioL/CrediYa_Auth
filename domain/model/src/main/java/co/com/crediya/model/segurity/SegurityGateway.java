package co.com.crediya.model.segurity;

import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.segurity.dto.TokenClaims;

public interface SegurityGateway {

    Token generateToken(TokenClaims tokenClaims);

    Claismo validateTokenClaims(Token token);
}
