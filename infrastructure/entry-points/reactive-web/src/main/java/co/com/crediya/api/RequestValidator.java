package co.com.crediya.api;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> userValidate(T dto) {
     /*   return Mono.fromCallable(() -> {
            var errors = new BeanPropertyBindingResult(dto, dto.getClass().getName());
            validator
                    .validate(dto, errors);
            if (errors.hasErrors()) {
                throw new ValidationException(errors.toString());
            }
            return dto;
        });*/
return Mono.empty();
    }


}
