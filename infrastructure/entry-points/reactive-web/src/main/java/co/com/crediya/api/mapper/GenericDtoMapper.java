package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.rol.RolDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.model.login.Login;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenericDtoMapper {
    User toUser(UserDto userDto);
    UserDto toDto(User user);

    User toUser(UserDocumentDto userDocumentDto);

    Login toUser(LoginDto userDto);

    Rol toRol(RolDto rolDto);
    RolDto toDto(Rol rol);
}
