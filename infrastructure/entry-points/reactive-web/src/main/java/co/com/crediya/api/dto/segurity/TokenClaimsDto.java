package co.com.crediya.api.dto.segurity;

public record TokenClaimsDto(
        String userId,
        String firstName,
        String lastName,
        String documentId,
        String email,
        String rolName
) {
}
