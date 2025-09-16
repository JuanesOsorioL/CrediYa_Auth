package co.com.crediya.model.segurity.dto;

public record TokenClaims(
        String userId,
        String firstName,
        String lastName,
        String documentId,
        String email,
        String rolName
) {
}
