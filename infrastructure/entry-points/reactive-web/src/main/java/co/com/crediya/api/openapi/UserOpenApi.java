package co.com.crediya.api.openapi;


import co.com.crediya.api.dto.UserDto;
import co.com.crediya.model.user.User;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

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
                                .schema(schemaBuilder().implementation(User.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                        .description("Error interno")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(ErrorResponse.class))));
    }
}