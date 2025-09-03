package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.LoginDto;
import co.com.crediya.api.dto.UserDocumentDto;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(User user);

    User toUser(UserDto userDto);

    User toUser(UserDocumentDto userDocumentDto);

    Login toUserL(LoginDto userDto);


}
