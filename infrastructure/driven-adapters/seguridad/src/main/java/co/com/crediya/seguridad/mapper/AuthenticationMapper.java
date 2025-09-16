package co.com.crediya.seguridad.mapper;

import co.com.crediya.model.segurity.dto.Claismo;
import io.jsonwebtoken.Claims;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {
    @Mapping(target = "FistName", expression = "java(claims.get(\"FistName\", String.class))")
    @Mapping(target = "LastName", expression = "java(claims.get(\"LastName\", String.class))")
    @Mapping(target = "Document", expression = "java(claims.get(\"Document\", String.class))")
    @Mapping(target = "Rol",      expression = "java(claims.get(\"Rol\", String.class))")
    @Mapping(target = "sub",      expression = "java(claims.getSubject())")
    @Mapping(target = "jti",      expression = "java(claims.getId())")
    @Mapping(target = "iat",      expression = "java(claims.getIssuedAt()  == null ? null : String.valueOf(claims.getIssuedAt().toInstant().getEpochSecond()))")
    @Mapping(target = "exp",      expression = "java(claims.getExpiration() == null ? null : String.valueOf(claims.getExpiration().toInstant().getEpochSecond()))")
    Claismo toClaismo(Claims claims);
}
