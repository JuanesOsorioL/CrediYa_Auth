package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.UserDocumentDto;
import co.com.crediya.api.dto.UserDto;
import co.com.crediya.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(User user);

    User toUser(UserDto userDto);

    User toUser(UserDocumentDto userDocumentDto);

}
