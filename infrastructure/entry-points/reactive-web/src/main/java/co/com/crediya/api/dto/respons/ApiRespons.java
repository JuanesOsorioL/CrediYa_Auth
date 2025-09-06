package co.com.crediya.api.dto.respons;

import lombok.Builder;

@Builder
public record ApiRespons<T>(
        Integer status,
        String message,
        T body
) {
}
