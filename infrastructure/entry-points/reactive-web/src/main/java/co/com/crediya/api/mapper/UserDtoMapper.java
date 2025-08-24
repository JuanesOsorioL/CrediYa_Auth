package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateUserDto;
import co.com.crediya.api.dto.EditUserDto;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(User user);
    User toUser (CreateUserDto createUserDto);
    List<UserDto> toDto(List<User> users);
    User toUser(EditUserDto editUserDto);


}
