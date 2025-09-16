package co.com.crediya.api.openapi;


import co.com.crediya.api.dto.login.LoginDto;
import co.com.crediya.api.dto.segurity.ClaismoDto;
import co.com.crediya.api.dto.user.EmailsRequestDto;
import co.com.crediya.api.dto.user.UserDocumentDto;
import co.com.crediya.api.dto.user.UserDto;
import co.com.crediya.api.response.UsersByEmailResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.core.fn.builders.securityrequirement.Builder.securityRequirementBuilder;

@UtilityClass
public class UserOpenApi {


    public Builder createUser(Builder builder) {
        return builder
                .operationId("createUser")
                .description("Crea un nuevo usuario en el sistema")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(UserDto.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.CREATED.value()))
                        .description("Usuario creado exitosamente")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(UserDto.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .description("Petición inválida")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))));
    }


    public Builder findAll(Builder builder) {
        return builder
                .operationId("findAll")
                .description("Obtiene todos los usuarios registrados")
                .tag("User")
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value()))
                        .description("Operación exitosa")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(UserDto[].class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                        .description("Error interno")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))));
    }


    public Builder findByDocumentId(Builder builder) {
        return builder
                .operationId("findByDocumentId")
                .description("buscar un usuario por documento")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder()
                                        .implementation(UserDocumentDto.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.OK.value()))
                        .description("Usuario encontrado exitosamente")
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder()
                                        .implementation(UserDto.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .description("Usuario no encontrado")
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .description("Petición inválida")
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder()
                                        .implementation(ErrorResponse.class))));
    }


    public Builder someUsers(Builder builder) {
        return builder
                .operationId("getUsersMapEmails")
                .description("Consulta algunos usuarios con base a una lista de correos")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(EmailsRequestDto.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value()))
                        .description("Usuarios recuperados correctamente")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(UsersByEmailResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .description("La lista de correos no puede ser vacía")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))));
    }


    public Builder login(Builder builder) {
        return builder
                .operationId("login")
                .description("Autentica al usuario y genera un token JWT")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(LoginDto.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.OK.value()))
                        .description("Usuario logueado exitosamente")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(String.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .description("Petición inválida")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
                        .description("Credenciales inválidas")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))));
    }


    public Builder validateToken(Builder builder) {
        return builder
                .operationId("validateToken")
                .description("Validar token JWT y devolver los claims")
                .tag("User")
                .parameter(parameterBuilder()
                        .name(HttpHeaders.AUTHORIZATION)
                        .in(ParameterIn.HEADER)
                        .required(true)
                        .description("Authorization: Bearer <token>")
                        .schema(schemaBuilder().implementation(String.class)))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.OK.value()))
                        .description("Token válido. Se envían claims")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ClaismoDto.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
                        .description("Token no proporcionado o inválido")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.FORBIDDEN.value()))
                        .description("Acceso denegado")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))))
                .security(securityRequirementBuilder().name("bearerAuth"));
    }
}