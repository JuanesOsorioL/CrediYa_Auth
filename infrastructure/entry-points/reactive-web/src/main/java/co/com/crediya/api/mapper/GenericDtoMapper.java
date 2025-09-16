package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.segurity.ClaismoDto;
import co.com.crediya.api.dto.segurity.TokenClaimsDto;
import co.com.crediya.api.dto.segurity.TokenDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.segurity.dto.Claismo;
import co.com.crediya.model.segurity.dto.Token;
import co.com.crediya.model.segurity.dto.TokenClaims;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenericDtoMapper {

    Login toLogin(LoginDto userDto);

    User toUser(UserDocumentDto userDocumentDto);
    // no se pidio, trae todos los usuarios
    UserDto toDto(User user);

    User toUser(UserDto userDto);

    //seguridad
    TokenClaims toTokenClaims(TokenClaimsDto tokenClaimsDto);
    TokenDto toTokenDto(Token token);
    Token toToken(TokenDto tokenDto);
    ClaismoDto toClaismoDto(Claismo claismo);

}