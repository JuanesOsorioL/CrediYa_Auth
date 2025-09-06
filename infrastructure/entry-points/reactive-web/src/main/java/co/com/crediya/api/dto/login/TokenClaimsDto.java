package co.com.crediya.api.dto.login;

public record TokenClaimsDto(
        String userId,
        String firstName,
        String lastName,
        String documentId,
        String email,
        String rolName
) {
}
