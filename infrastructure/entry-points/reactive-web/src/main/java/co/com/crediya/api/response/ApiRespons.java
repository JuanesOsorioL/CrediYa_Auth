package co.com.crediya.api.response;

import lombok.Builder;


@Builder
public record ApiRespons<T>(
        Integer status,
        String code,
        String message,
        T body
) {
}
